package net.holybee.tarot

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.ktor.util.reflect.instanceOf
import net.holybee.tarot.databinding.FragmentReadingDisplayBinding
import net.holybee.tarot.databinding.FragmentTarotQuestionBinding

class ReadingDisplayFragment : Fragment() {
    private var _binding: FragmentReadingDisplayBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val args: ReadingDisplayFragmentArgs by navArgs()

    companion object {
    }

    private lateinit var viewModel: ReadingDisplayViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReadingDisplayBinding.inflate(inflater, container, false)
        binding.readingText.movementMethod=ScrollingMovementMethod()
        binding.readingText.text = args.readingText

        return binding.root


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.back_only, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {


            R.id.rate_app -> {
                Dialogs.rateApp(this)
                true
            }
            R.id.navigate_back -> {
                requireActivity().onBackPressed()
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[ReadingDisplayViewModel::class.java]
        // TODO: Use the ViewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}