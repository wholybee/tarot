package net.holybee.tarot

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import net.holybee.tarot.databinding.FragmentLogonBinding
import net.holybee.tarot.holybeeAPI.AccountInformation
import net.holybee.tarot.holybeeAPI.CreateAccountResponseListener
import net.holybee.tarot.holybeeAPI.HolybeeAPIClient
import net.holybee.tarot.holybeeAPI.HolybeeURL
import net.holybee.tarot.holybeeAPI.LoginResponseListener
import java.util.Locale

private const val TAG = "AccountFragment"
class LogonFragment : Fragment(), LoginResponseListener, CreateAccountResponseListener {

    private lateinit var viewModel: LogonViewModel

    private var _binding: FragmentLogonBinding? = null
    private val binding
        get()=checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val handler = Handler(Looper.getMainLooper())

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myActionbar = (requireActivity() as AppCompatActivity).supportActionBar
        myActionbar?.setDisplayHomeAsUpEnabled(true)
        myActionbar?.show()
        setHasOptionsMenu(true)
        // Get a reference to the Application object
        val application = requireActivity().application

        // Initialize the ViewModel with the Application object
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))[LogonViewModel::class.java]
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.back_only, menu)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.open_account -> {
                findNavController().navigate(
                    ThreeQuestionFragmentDirections.actionToAccountFragment())
                true
            }
            R.id.open_buyCoins -> {
                navigatePurchase()
                true
            }
            R.id.rate_app -> {
                Dialogs.rateApp(this)
                true
            }
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }
    private fun navigatePurchase () {
        findNavController().navigate(
            LogonFragmentDirections.actionToPurchaseFragment())
    }

        override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogonBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[LogonViewModel::class.java]

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            clickLogin()
        }

        binding.createAccountButton.setOnClickListener {
            clickNewAccount()
        }


        binding.logoutButton.setOnClickListener {
            clickLogout()
        }

        binding.loginOrRegisterTextView.setOnClickListener {

            binding.emailEditText.isVisible=true
            binding.createAccountButton.isVisible=true
            binding.loginButton.isVisible=false
            binding.haveAccountTextView.isVisible=true
            binding.loginOrRegisterTextView.isVisible=false
        }

        binding.haveAccountTextView.setOnClickListener {
            setVisibility()
            binding.emailEditText.isVisible=false
            binding.createAccountButton.isVisible=false
            binding.loginButton.isVisible=true
            binding.haveAccountTextView.isVisible=false
            binding.loginOrRegisterTextView.isVisible=true
        }

        binding.showHideButton.setOnClickListener{
            clickShowHide()
        }

        binding.forgotPasswordbutton.setOnClickListener {
            openWebpage(HolybeeURL.forgotPasswordURL)
        }

        setVisibility()
    }


    private fun setVisibility() {
        if (AccountInformation.isLoggedIn) {
            binding.usernameEditText.setText(AccountInformation.username)
            binding.emailEditText.setText(AccountInformation.email)
            binding.usernameEditText.isEnabled = false
            binding.forgotPasswordbutton.isVisible = false

            binding.loginButton.isVisible=false
            binding.logoutButton.isVisible=true
            binding.createAccountButton.isVisible=false

            binding.loginOrRegisterTextView.isVisible=false
            binding.passwordEditText.isVisible=false
            binding.showHideButton.isVisible=false
            binding.emailEditText.isVisible=false
            binding.haveAccountTextView.isVisible=false

        } else {
            binding.usernameEditText.isEnabled = true
            binding.logoutButton.isVisible=false
            binding.loginButton.isVisible=false
            binding.createAccountButton.isVisible=true
            binding.forgotPasswordbutton.isVisible = true
            binding.loginOrRegisterTextView.isVisible=false
            binding.passwordEditText.isVisible=true
            binding.showHideButton.isVisible=true
            binding.emailEditText.isVisible=true
            binding.haveAccountTextView.isVisible=true

        }
    }


    @Suppress("DEPRECATION")
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun clickShowHide() {
        if(binding.showHideButton.tag=="hidden") {
            binding.passwordEditText.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            binding.showHideButton.setImageDrawable(
                resources.getDrawable(R.drawable.ic_password_visible))
            binding.showHideButton.tag = "visible"
        } else{
            binding.passwordEditText.transformationMethod =
                PasswordTransformationMethod.getInstance()
            binding.showHideButton.setImageDrawable(
                resources.getDrawable(R.drawable.ic_password_hidden))
            binding.showHideButton.tag = "hidden"
        }
    }

    private fun clickLogin ()  {

        val username = binding.usernameEditText.text.toString().lowercase(Locale.US).trim()
        val password = binding.passwordEditText.text.toString().trim()
        AccountInformation.username = username
        AccountInformation.password = password
        if (!loginParametersValid(username,password)) return
        val client = HolybeeAPIClient
        client.loginAsync(username, password, this)

    }

    override fun onLoginSuccess(userToken: String, coins:Int) {
        val applicationInstance = activity?.application
        if (applicationInstance != null) {
            AccountInformation.saveLoginInfo(applicationInstance, userToken)

        }

        handler.post {
            Toast.makeText(context,"Login Successful",Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            setVisibility()
        }
    }

    override fun onLoginFail(result: String) {
        handler.post {
            Toast.makeText(context, "Failed login:$result", Toast.LENGTH_LONG).show()
        }
    }


    private fun clickNewAccount () {

        val username = binding.usernameEditText.text.toString().lowercase(Locale.US).trim()
        val password = binding.passwordEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().lowercase(Locale.US).trim()

        if (!loginParametersValid(username,password,email)) return

        val client = HolybeeAPIClient
        AccountInformation.username = username
        AccountInformation.email = email
        AccountInformation.password = password

        client.createAccountAsync(requireContext(), username, password, email, this)
    }

    override fun onAccountCreateSuccess(userToken: String, coins: Int) {
        val applicationInstance = activity?.application
        if (userToken.length > 20) {
            Log.i(TAG,"Account Created")
            Log.i(TAG,userToken)
            if (applicationInstance != null) {
                AccountInformation.saveLoginInfo(applicationInstance, userToken)
            }

            handler.post {
                Toast.makeText(context, "Account created.", Toast.LENGTH_LONG).show()
                findNavController().popBackStack()
                setVisibility()
            }
        } else {
            handler.post {
                Toast.makeText(context, "No token returned.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onAccountCreateFail(result: String) {
        handler.post {
            Toast.makeText(context, "Failed: $result", Toast.LENGTH_LONG).show()
        }
    }

    private fun loginParametersValid (
        username:String,
        password:String,
        email:String = "example@holybee.net"
    ) : Boolean {
        var toastText:String? = null

        if (username.length < 5) {
            toastText = "Username must be at least 5 characters."
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toastText = "Invalid email address."
            Log.e(TAG,"Email:$email")
        } else if (password.length < 8) {
            toastText = "Password must be at least 8 characters."
        }

        if (toastText != null) {
            Toast.makeText(context,toastText,Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun clickLogout () {
        val applicationInstance = activity?.application
        if (applicationInstance != null) {
            AccountInformation.logout(applicationInstance)
            Toast.makeText(context,"Logout Successful.",Toast.LENGTH_LONG).show()
            setVisibility()
        }
    }
    private fun openWebpage (url : String) {


        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        try {
            // if (intent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(intent)
        } catch (e:Exception) { Log.e(TAG,"Failed to open web browser.")}
        // }
    }

}