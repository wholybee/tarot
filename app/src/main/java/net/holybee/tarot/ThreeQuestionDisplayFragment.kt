package net.holybee.tarot

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
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.navArgs
import net.holybee.tarot.databinding.FragmentThreeQuestionDisplayBinding

class ThreeQuestionDisplayFragment : Fragment() {
    private var _binding: FragmentThreeQuestionDisplayBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private val args: ThreeQuestionDisplayFragmentArgs by navArgs()

    companion object {
    }

    private lateinit var viewModel: ThreeQuestionDisplayViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThreeQuestionDisplayBinding.inflate(inflater, container, false)
        binding.readingText.movementMethod=ScrollingMovementMethod()
        binding.readingText.text = args.readingText

        return binding.root


    }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
   //     myActionbar = (requireActivity() as AppCompatActivity).supportActionBar
  //      myActionbar?.setDisplayHomeAsUpEnabled(true)
  //      myActionbar?.show()
        setHasOptionsMenu(true)
    }
    @Suppress("DEPRECATION")
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
            android.R.id.home -> {
                requireActivity().onBackPressed()
                true
            }
            else->super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[ThreeQuestionDisplayViewModel::class.java]
        // TODO: Use the ViewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
 //       (requireActivity() as AppCompatActivity)
 //           .supportActionBar?.hide()
        _binding = null
    }

}