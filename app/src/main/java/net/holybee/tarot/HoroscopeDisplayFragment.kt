package net.holybee.tarot

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHoroscopeDisplayBinding.inflate(inflater, container, false)
        binding.readingText.text = args.reading
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HoroscopeDisplayViewModel::class.java)
        // TODO: Use the ViewModel
    }

}