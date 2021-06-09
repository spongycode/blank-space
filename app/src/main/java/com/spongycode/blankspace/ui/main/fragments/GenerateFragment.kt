import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.spongycode.blankspace.R
import com.spongycode.blankspace.databinding.FragmentGenerateBinding
import com.spongycode.blankspace.ui.main.TextEditorDialogFragment
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.ViewType


@Suppress("DEPRECATION")
class GenerateFragment : Fragment() {

    private var _binding: FragmentGenerateBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGenerateBinding.inflate(inflater, container, false)


        val mPhotoEditorView: PhotoEditorView = binding.photoEditorView

        mPhotoEditorView.source.setImageResource(R.drawable.croc)


        val mTextRobotoTf = ResourcesCompat.getFont(requireContext(), R.font.opensans)
        val mEmojiRobotoTf = ResourcesCompat.getFont(requireContext(), R.font.opensans)


        val mPhotoEditor = PhotoEditor.Builder(requireContext(), mPhotoEditorView)
                .setPinchTextScalable(true)
                .setDefaultTextTypeface(mTextRobotoTf)
                .setDefaultEmojiTypeface(mEmojiRobotoTf)
                .build()


        binding.memeUndo.setOnClickListener {
            mPhotoEditor.undo()
        }

        binding.memeRedo.setOnClickListener {
            mPhotoEditor.redo()
        }


        binding.memeAddText.setOnClickListener {
            mPhotoEditor.addText("Hold to Edit", resources.getColor(R.color.purple_700))
        }




        mPhotoEditor.setOnPhotoEditorListener(object : OnPhotoEditorListener {

            override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {
                val textEditorDialogFragment = TextEditorDialogFragment.show(requireActivity() as AppCompatActivity, text!!, colorCode)
                textEditorDialogFragment.setOnTextEditorListener { inputText, colorCode ->
                    val styleBuilder = TextStyleBuilder()
                    styleBuilder.withTextColor(colorCode)
                    mPhotoEditor.editText(rootView!!, inputText, styleBuilder)
                }
            }

            override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
                Unit
            }

            override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
                Unit
            }

            override fun onStartViewChangeListener(viewType: ViewType?) {
                Unit
            }

            override fun onStopViewChangeListener(viewType: ViewType?) {
                Unit
            }
        })

        return binding.root
    }
}