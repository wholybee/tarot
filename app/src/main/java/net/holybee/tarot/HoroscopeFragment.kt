package net.holybee.tarot

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import net.holybee.tarot.databinding.FragmentHoroscopeBinding
import net.holybee.tarot.databinding.FragmentLogonBinding
import net.holybee.tarot.holybeeAPI.AccountInformation
import java.time.LocalDate

private const val TAG = "HoroscopeFragment"

class HoroscopeFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private var _binding: FragmentHoroscopeBinding? = null
    private val binding
        get()=checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"

        }
    companion object {
        fun newInstance() = HoroscopeFragment()
    }

    private lateinit var viewModel: HoroscopeViewModel
    val years = mutableListOf<String>()
    val days = mutableListOf<String>()
    private val openAi = OpenAI_wlh
    private val modelIdHoroscope = "horoscope"


    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

    }

    override fun onResume() {
        super.onResume()
        val myActionbar = (requireActivity() as AppCompatActivity).supportActionBar
        myActionbar?.setDisplayHomeAsUpEnabled(true)
        myActionbar?.show()
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.child, menu)
    }

    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {


            R.id.open_buyCoins -> {
                navigatePurchase()
                true
            }
            R.id.rate_app -> {
                Dialogs.rateApp(this)
                true
            }

            android.R.id.home-> {
                requireActivity().onBackPressed()
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }

    private fun navigatePurchase () {
        findNavController().navigate(
            HoroscopeFragmentDirections.actionHoroscopeFragmentToPurchaseFragment())
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHoroscopeBinding.inflate(inflater, container, false)
        binding.horoscopeButton.setOnClickListener { clickButton() }
        binding.monthSpinner.onItemSelectedListener = this
        binding.daySpinner.onItemSelectedListener = this
        for (i in 1900..2030) {
            years.add(i.toString())
        }
        for (i in 1..31) {
            days.add(i.toString())
        }
        val monthAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.months,
            android.R.layout.simple_list_item_1, )

        val yearAdapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, years  )
        val dayAdapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, days  )
        binding.daySpinner.adapter=dayAdapter
        binding.yearSpinner.adapter=yearAdapter
        binding.monthSpinner.adapter=monthAdapter
        binding.yearSpinner.setSelection(years.indexOf("2023"))
        return binding.root
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent?.id) {
            binding.monthSpinner.id -> {
                Log.i(TAG,"Month ${position+1}")
                viewModel.month = position + 1
            }
            binding.daySpinner.id -> {
                Log.i(TAG,"Day ${position+1}")
                viewModel.day = position +1
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        // TODO("Not yet implemented")
    }

    private fun monthItem() {
        TODO("Not yet implemented")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HoroscopeViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun clickButton() {
        val name = binding.nameEditText.text
        val sex = if (binding.manRadioButton.isChecked) "Man" else "Woman"
        val sign = getAstrologicalSign(viewModel.month, viewModel.day, 1974)
        Log.i(TAG,sign)
        val prompt = "The subjects name is $name. $name is a $sex and his sign is $sign."
        showHoroscope(prompt)
       }

    fun getAstrologicalSign(month: Int, day: Int, year: Int): String {


        return when {
            month == 12 && day >= 22 || month == 1 && day <= 19 -> "Capricorn"
            month == 1 && day >= 20 || month == 2 && day <= 18 -> "Aquarius"
            month == 2 && day >= 19 || month == 3 && day <= 20 -> "Pisces"
            month == 3 && day >= 21 || month == 4 && day <= 19 -> "Aries"
            month == 4 && day >= 20 || month == 5 && day <= 20 -> "Taurus"
            month == 5 && day >= 21 || month == 6 && day <= 20 -> "Gemini"
            month == 6 && day >= 21 || month == 7 && day <= 22 -> "Cancer"
            month == 7 && day >= 23 || month == 8 && day <= 22 -> "Leo"
            month == 8 && day >= 23 || month == 9 && day <= 22 -> "Virgo"
            month == 9 && day >= 23 || month == 10 && day <= 22 -> "Libra"
            month == 10 && day >= 23 || month == 11 && day <= 21 -> "Scorpio"
            month == 11 && day >= 22 || month == 12 && day <= 21 -> "Sagittarius"
            else -> "Unknown"
        }
    }


    private fun showHoroscope(prompt: String) {
        binding.progressBar.visibility = View.VISIBLE
     //   disableAllButtons()
        AccountInformation.ratingCount+=1

        lifecycleScope.launch {
            val response = openAi.askGPT(prompt, modelIdHoroscope)
            binding.progressBar.visibility = View.INVISIBLE
            if (response.status=="OK") {
                AccountInformation.coins.postValue( AccountInformation.coins.value?.minus(1))
                findNavController().navigate(
                    HoroscopeFragmentDirections.actionToHoroscopeDisplayFragment(response.message)
                    )

            } else {
                Toast.makeText(context,"Error:\n${response.message}", Toast.LENGTH_LONG).show()

            }
        }
    }
}