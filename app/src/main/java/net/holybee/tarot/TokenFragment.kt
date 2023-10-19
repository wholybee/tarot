package net.holybee.tarot

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.aallam.openai.api.exception.AuthenticationException
import com.aallam.openai.api.model.Model
import com.aallam.openai.client.OpenAI
import kotlinx.coroutines.launch
import net.holybee.tarot.databinding.FragmentTokenBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val TAG ="TokenFragment"


/**
 * A simple [Fragment] subclass.
 * Use the [TokenFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TokenFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentTokenBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            Log.d(TAG, "beforeTextChanged")
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if ((binding.tokenText.text.toString() == PreferencesDb.token) && binding.tokenText.length() > 0) {
                binding.okButton.isEnabled = true
                binding.validateButton.isEnabled = false
            } else {
                binding.okButton.isEnabled = false
                binding.validateButton.isEnabled = true
            }
        }

        override fun afterTextChanged(s: Editable?) {
            Log.d(TAG, "afterTextChanged")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        _binding = FragmentTokenBinding.inflate(inflater, container, false)

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TokenFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TokenFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        enableButtons()
        Log.d(TAG,"onActivityCreated")
        println(PreferencesDb.token)
        binding.tokenText.setText(PreferencesDb.token)
    }

    fun enableButtons() {
        binding.let {
            it.okButton.setOnClickListener { clickOk() }
            it.okButton.isEnabled = false
            it.validateButton.setOnClickListener { clickValidate() }
            it.tokenText.addTextChangedListener(textWatcher)
        }
    }

    fun clickOk() {
        val key = binding.tokenText.text.toString()
        PreferencesDb.token = key
        findNavController().popBackStack()
    }

    fun clickValidate () {
        val key = binding.tokenText.text.toString()


        lifecycleScope.launch {
            try {
                val models: List<Model> = OpenAI(token=key).models()
                models.forEach {
                    println(it.id.id) }
                binding.okButton.isEnabled = true
                binding.validateButton.isEnabled = false
                Toast.makeText(requireContext(), "Key Valid.", Toast.LENGTH_LONG).show()


            } catch (e: AuthenticationException) {
                println("Authentication Failed")
                binding.okButton.isEnabled = false
                Toast.makeText(requireContext(), "Key invalid!", Toast.LENGTH_LONG).show()
            }

        }
    }


}