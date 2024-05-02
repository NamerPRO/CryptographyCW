package ru.namerpro.nchat.commons

import androidx.lifecycle.MutableLiveData

class QueuedLiveData<T> : MutableLiveData<T>() {

    private val queue = ArrayDeque<T>()

    override fun setValue(value: T) {
        synchronized(queue) {
            super.setValue(value)
            queue.removeFirst()
            if (queue.isNotEmpty()) {
                queue.first().run {
                    super.postValue(this)
                }
            }
        }
    }

    override fun postValue(value: T) {
        synchronized(queue) {
            queue.addLast(value)
            if (queue.size == 1) {
                super.postValue(value)
            }
        }
    }

}