package ru.namerpro.nchat.domain.entities.ciphers.context

import ru.namerpro.nchat.domain.entities.ciphers.context.encrypter.Encrypter
import ru.namerpro.nchat.domain.entities.ciphers.encryptionstate.EncryptionState
import ru.namerpro.nchat.domain.entities.ciphers.mode.Mode
import ru.namerpro.nchat.domain.entities.ciphers.mode.impl.CBC
import ru.namerpro.nchat.domain.entities.ciphers.mode.impl.CFB
import ru.namerpro.nchat.domain.entities.ciphers.mode.impl.CTR
import ru.namerpro.nchat.domain.entities.ciphers.mode.impl.ECB
import ru.namerpro.nchat.domain.entities.ciphers.mode.impl.OFB
import ru.namerpro.nchat.domain.entities.ciphers.mode.impl.PCBC
import ru.namerpro.nchat.domain.entities.ciphers.mode.impl.RD
import ru.namerpro.nchat.domain.entities.ciphers.padding.Padding
import ru.namerpro.nchat.domain.entities.ciphers.padding.impl.ANSIX923
import ru.namerpro.nchat.domain.entities.ciphers.padding.impl.ISO10126
import ru.namerpro.nchat.domain.entities.ciphers.padding.impl.PKCS7
import ru.namerpro.nchat.domain.entities.ciphers.padding.impl.Zeros
import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.SymmetricEncrypter
import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.modes.SymmetricEncryptMode
import ru.namerpro.nchat.domain.entities.ciphers.symmetricapi.modes.SymmetricPaddingMode
import ru.namerpro.nchat.domain.entities.ciphers.symmetricencrypters.magenta.Magenta
import ru.namerpro.nchat.domain.entities.ciphers.symmetricencrypters.rc5.RC5
import ru.namerpro.nchat.domain.model.Task
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.min

class SymmetricEncrypterContext(
    encrypter: Encrypter,
    key: ByteArray,
    mode: Mode,
    padding: Padding,
    iv: ByteArray?,
    vararg options: Any
) : AutoCloseable {
    private val service: ExecutorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
    private var mode: SymmetricEncryptMode? = null
    private val padding: SymmetricPaddingMode
    private var encrypter: SymmetricEncrypter? = null
    private var blockSize = 0

    init {
        when (encrypter) {
            Encrypter.MAGENTA -> {
                require(options.size < 2) { "Expected to see either primitive element of GF(2^8) passed as optional argument or nothing in case you want standard primitive element ot be applied, but more then one element passed found!" }
                val alpha = if (options.isEmpty()) 2u else (options[0] as UByte)
                this.encrypter = Magenta(key, alpha)
                this.blockSize = 16
            }

            Encrypter.RC5 -> {
                require(key.size <= 2040) { "Key length cannot be bigger than 2040 bits but ${key.size} found!" }
                require(options.size < 4) { "Expected to see 3 arguments passed as optional argument: w, r and b coming respectively, but some (or all) of them cannot be found." }
                this.encrypter = RC5(
                    options[0] as UByte,
                    options[1] as UByte,
                    options[2] as UByte,
                    key
                )
                this.blockSize = (options[0] as UByte).toInt() / 4
            }
        }
        if (iv == null) {
            if (mode === Mode.ECB) {
                this.mode = ECB(service)
            } else {
                throw IllegalArgumentException(("No initialization vector passed, but it's required for '" + mode.name) + "' encrypt mode!")
            }
        } else {
            when (mode) {
                Mode.CBC -> this.mode = CBC(service, iv)
                Mode.PCBC -> this.mode = PCBC(service, iv)
                Mode.OFB -> this.mode = OFB(iv)
                Mode.CFB -> this.mode = CFB(service, iv)
                Mode.CTR -> this.mode = CTR(service, iv, this.blockSize)
                Mode.RD -> this.mode = RD(service, iv)
                else -> throw IllegalArgumentException(("byte[] IV can only be passed in pair with any of the following encrypt modes: CBC, PCBC, OFB, CFB, CTR, RD, - but '" + mode.name) + "' found!")
            }
        }

        this.padding = when (padding) {
            Padding.PKCS7 -> PKCS7()
            Padding.ZEROS -> Zeros()
            Padding.ISO_10126 -> ISO10126()
            Padding.ANSI_X_923 -> ANSIX923()
        }
    }

    fun encrypt(
        src: ByteArray
    ): CompletableFuture<ByteArray> {
        return CompletableFuture.supplyAsync {
            val paddedSrc: ByteArray = padding.add(src, blockSize)
            mode!!.apply(paddedSrc, blockSize, encrypter!!)
        }
    }

    fun encrypt(
        task: Task,
        input: InputStream,
        output: RandomAccessFile,
        size: Long
    ): CompletableFuture<EncryptionState> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync fileEncryptionDecryptionCore(task, input, output, size, true)
        }
    }

    fun decrypt(
        src: ByteArray
    ): CompletableFuture<ByteArray> {
        return CompletableFuture.supplyAsync {
            val paddedSrc: ByteArray = mode!!.reverse(src, blockSize, encrypter!!)
            padding.remove(paddedSrc, blockSize)
        }
    }

    fun decrypt(
        task: Task,
        input: InputStream,
        output: RandomAccessFile,
        size: Long
    ): CompletableFuture<EncryptionState> {
        return CompletableFuture.supplyAsync {
            return@supplyAsync fileEncryptionDecryptionCore(task, input, output, size, false)
        }
    }

    private fun fileEncryptionDecryptionCore(
        task: Task,
        input: InputStream,
        output: RandomAccessFile,
        size: Long,
        isEncrypting: Boolean
    ): EncryptionState {
        try {
            val potionSize = MEMORY_LIMIT_SIZE * blockSize
            val buffer = ByteArray(potionSize)
            var position = 0L ; var length: Int
            while (input.read(buffer).also { length = it } > 0) {
                if (task.isCancelled) {
                    return EncryptionState.Cancelled
                }
                val isLast = (position + potionSize) >= size
                val sendableBuffer = if (!isLast) buffer else buffer.take(length).toByteArray()
                val parts = runPartialEncryption(task, position, sendableBuffer, length.toLong(), isEncrypting, isLast)
                if (task.isCancelled) {
                    return EncryptionState.Cancelled
                }
                collectPartsToEncryptedFile(task, output, parts)
                if (task.isCancelled) {
                    return EncryptionState.Cancelled
                }
                position += potionSize
                val progressToAdd = 1.0 * PROGRESS_SECTION_THAT_TAKES_ENCRYPTION * min(length.toLong(), size) / size
                task.progress.invoke(progressToAdd)
            }
            return EncryptionState.Success
        } catch (error: Throwable) {
            when (error) {
                is IOException, is ExecutionException, is InterruptedException -> {
                    Thread.currentThread().interrupt()
                    return EncryptionState.Error(error)
                }
            }
            throw error
        }
    }

    private fun collectPartsToEncryptedFile(
        task: Task,
        output: RandomAccessFile,
        parts: HashSet<Pair<Long, CompletableFuture<ByteArray>>>
    ) {
        var it = parts.iterator()
        while (it.hasNext()) {
            if (task.isCancelled) {
                return
            }
            val data = it.next()
            if (data.second.isDone) {
                val dataBuffer = data.second.get()
                val offset = data.first
                output.seek(offset)
                output.write(dataBuffer)
                it.remove()
            }
            if (!it.hasNext()) {
                it = parts.iterator()
            }
        }
    }

    private fun runPartialEncryption(
        task: Task,
        startOffset: Long,
        byteBuffer: ByteArray,
        size: Long,
        isEncrypting: Boolean,
        isLast: Boolean
    ): HashSet<Pair<Long, CompletableFuture<ByteArray>>> {
        val potionSize = AMOUNT_OF_BLOCKS_TO_READ * blockSize
        var length: Int; var position = 0L
        val buffer = ByteArray(potionSize)
        val parts = hashSetOf<Pair<Long, CompletableFuture<ByteArray>>>()
        val input = byteBuffer.inputStream()
        while (input.read(buffer).also { length = it } > 0) {
            if (task.isCancelled) {
                return hashSetOf()
            }
            val data = if (!isLast || position + length + potionSize < size) {
                val taskBuffer = buffer.clone()
                if (isEncrypting) CompletableFuture.supplyAsync { mode!!.apply(taskBuffer, blockSize, encrypter!!) }
                    else CompletableFuture.supplyAsync { mode!!.reverse(taskBuffer, blockSize, encrypter!!) }
            } else {
                val endBuffer = ByteArray(potionSize)
                val endLength = input.read(endBuffer)
                if (isEncrypting) {
                    if (endLength == -1) encrypt(buffer.take(length).toByteArray())
                        else encrypt(buffer + endBuffer.take(endLength).toByteArray())
                } else {
                    if (endLength == -1) decrypt(buffer.take(length).toByteArray())
                        else decrypt(buffer + endBuffer.take(endLength).toByteArray())
                }
            }
            parts.add(Pair(position + startOffset, data))
            position += potionSize
        }
        return parts
    }

    override fun close() {
        service.shutdown()
        service.shutdownNow()
    }

    companion object {
        private const val AMOUNT_OF_BLOCKS_TO_READ = 256
        private const val MEMORY_LIMIT_SIZE = 1024 * 256
        private const val PROGRESS_SECTION_THAT_TAKES_ENCRYPTION = 50
    }
}