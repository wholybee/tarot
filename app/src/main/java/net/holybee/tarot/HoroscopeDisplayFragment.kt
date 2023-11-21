package net.holybee.tarot

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.navArgs
import net.holybee.tarot.databinding.FragmentHoroscopeBinding
import net.holybee.tarot.databinding.FragmentHoroscopeDisplayBinding

class HoroscopeDisplayFragment : Fragment() {

    private var _binding: FragmentHoroscopeDisplayBinding? = null
    private val binding
        get()=checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"

        }
    private val args: HoroscopeDisplayFragmentArgs by navArgs()
    companion object {
        fun newInstance() = HoroscopeDisplayFragment()
    }

    private lateinit var viewModel: HoroscopeDisplayViewModel

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHoroscopeDisplayBinding.inflate(inflater, container, false)
        binding.readingText.text = args.reading
        return binding.root
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HoroscopeDisplayViewModel::class.java)
        // TODO: Use the ViewModel
    }

}