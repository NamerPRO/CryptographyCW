package ru.namerpro.nchat.ui.root

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.namerpro.nchat.R
import ru.namerpro.nchat.commons.Constants
import ru.namerpro.nchat.commons.showEditableDialog
import ru.namerpro.nchat.commons.showUnclosableDialog
import ru.namerpro.nchat.databinding.ActivityRootBinding

class RootActivity : AppCompatActivity() {

    private var binding: ActivityRootBinding? = null

    private val viewModel: RootViewModel by viewModel()

    private var initializationInProgressDialog: AlertDialog? = null

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        binding = ActivityRootBinding.inflate(layoutInflater, )
        setContentView(binding?.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.rootFragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        binding?.bottomNavigationView.let {
            it?.setupWithNavController(navController)
        }

        viewModel.observeApplicationState().observeForever {
            render(it)
        }

        initializeClientIfNot()

//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            when (destination.id) {
//
//            }
//        }
    }

    private fun initializeClientIfNot() {
        if (RootViewModel.CLIENT_ID == Constants.FIELD_NOT_INITIALIZED) {
            showEditableDialog(this, getString(R.string.enter_username_title), getString(R.string.enter_username_message)) {
                initializationInProgressDialog = showUnclosableDialog(this, getString(R.string.initialization_in_progress_title), getString(R.string.initialization_in_progress_message))
                viewModel.initializeClient(it)
            }
        }
    }

    private fun render(
        state: ApplicationState
    ) {
        when (state) {
            is ApplicationState.FailedToPingNewChats -> Toast.makeText(this, getString(R.string.failed_to_ping_new_chats), Toast.LENGTH_SHORT).show()
            is ApplicationState.FailedToGetSecretKeys -> Toast.makeText(this, getString(R.string.failed_to_get_secret_keys), Toast.LENGTH_SHORT).show()
            is ApplicationState.ClientInitializationFailed -> {
                initializationInProgressDialog?.dismiss()
                Toast.makeText(this, getString(R.string.client_initialization_failed), Toast.LENGTH_SHORT).show()
                initializeClientIfNot()
            }
            is ApplicationState.ClientInitializationSuccess -> {
                initializationInProgressDialog?.dismiss()
                ru.namerpro.nchat.commons.showDialog(this, getString(R.string.successful_initialization_title), getString(R.string.successful_initialization_message))
            }
            is ApplicationState.IncorrectNameProvided -> {
                initializationInProgressDialog?.dismiss()
                Toast.makeText(this, getString(R.string.name_not_available), Toast.LENGTH_SHORT).show()
                initializeClientIfNot()
            }
        }
    }

}