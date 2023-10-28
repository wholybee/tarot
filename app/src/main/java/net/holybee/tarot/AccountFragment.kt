package net.holybee.tarot

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import net.holybee.tarot.databinding.FragmentAccountBinding
import net.holybee.tarot.holybeeAPI.AccountInformation
import net.holybee.tarot.holybeeAPI.CreateAccountResponseListener
import net.holybee.tarot.holybeeAPI.HolybeeAPIClient
import net.holybee.tarot.holybeeAPI.LoginResponseListener

private const val TAG = "AccountFragment"
class AccountFragment : Fragment(), LoginResponseListener, CreateAccountResponseListener {

    private lateinit var viewModel: AccountViewModel

    private var _binding: FragmentAccountBinding? = null
    private val binding
        get()=checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get a reference to the Application object
        val application = requireActivity().application

        // Initialize the ViewModel with the Application object
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))
            .get(AccountViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AccountViewModel::class.java)

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
            Log.i(TAG,"Register onClick")
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

        setVisibility()
    }


    private fun setVisibility() {
        if (AccountInformation.isLoggedIn) {
            binding.usernameEditText.setText(AccountInformation.username)
            binding.emailEditText.setText(AccountInformation.email)
            binding.usernameEditText.isEnabled = false


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

            binding.loginOrRegisterTextView.isVisible=false
            binding.passwordEditText.isVisible=true
            binding.showHideButton.isVisible=true
            binding.emailEditText.isVisible=true
            binding.haveAccountTextView.isVisible=true

        }
    }


    private fun clickShowHide() {
        if(binding.showHideButton.tag=="hidden") {
            binding.passwordEditText.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            binding.showHideButton.setImageDrawable(
                getResources().getDrawable(R.drawable.ic_password_visible))
            binding.showHideButton.tag = "visible"
        } else{
            binding.passwordEditText.transformationMethod =
                PasswordTransformationMethod.getInstance()
            binding.showHideButton.setImageDrawable(
                getResources().getDrawable(R.drawable.ic_password_hidden))
            binding.showHideButton.tag = "hidden"
        }
    }

    private fun clickLogin ()  {

        val username = binding.usernameEditText.text.toString().toLowerCase().trim()
        val password = binding.passwordEditText.text.toString().trim()
        AccountInformation.username = username
        AccountInformation.password = password
        if (!loginParametersValid(username,password)) return
        val client = HolybeeAPIClient
        client.loginAsync(username, password, this)

    }

    override fun onLoginSuccess(returnedToken: String, coins:Int) {
        val applicationInstance = activity?.application
        if (applicationInstance != null) {
            AccountInformation.saveLoginInfo(applicationInstance, returnedToken)

        }

        handler.post {
            Toast.makeText(context,"Login Succesful",Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            setVisibility()
        }
    }

    override fun onLoginFail(result: String) {
        handler.post {
            Toast.makeText(context, "Failed login:" + result, Toast.LENGTH_LONG).show()
        }
    }


    private fun clickNewAccount () {

        val username = binding.usernameEditText.text.toString().toLowerCase().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().toLowerCase().trim()

        if (!loginParametersValid(username,password,email)) return

        val client = HolybeeAPIClient
        AccountInformation.username = username
        AccountInformation.email = email
        AccountInformation.password = password

        client.createAccountAsync(username, password, email, this)
    }

    override fun onAccountCreateSuccess(returnedToken: String, coins: Int) {
        val applicationInstance = activity?.application
        if (returnedToken.length > 20) {
            Log.d(TAG,"Account Created")
            Log.d(TAG,returnedToken)
            if (applicationInstance != null) {
                AccountInformation.saveLoginInfo(applicationInstance, returnedToken)
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
            Toast.makeText(context, "Failed: " + result, Toast.LENGTH_LONG).show()
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

}