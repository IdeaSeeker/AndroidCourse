package com.nikitastroganov.androidcourse.ui.signup

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.CheckBox
import androidx.activity.addCallback
import androidx.core.text.buildSpannedString
import androidx.core.text.inSpans
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.nikitastroganov.androidcourse.R
import com.nikitastroganov.androidcourse.databinding.FragmentSignUpBinding
import com.nikitastroganov.androidcourse.ui.base.BaseFragment
import com.nikitastroganov.androidcourse.util.getSpannedString
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : BaseFragment(R.layout.fragment_sign_up) {

    private val viewBinding by viewBinding(FragmentSignUpBinding::bind)

    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            onBackButtonPressed()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewBinding.backButton.applyInsetter {
            type(statusBars = true) { margin() }
        }
        viewBinding.signUpButton.applyInsetter {
            type(navigationBars = true) { margin() }
        }
        viewBinding.backButton.setOnClickListener {
            onBackButtonPressed()
        }
        viewBinding.signUpButton.setOnClickListener {
            viewModel.signUp(
                firstName = viewBinding.firstnameEditText.text?.toString() ?: "",
                lastName = viewBinding.lastnameEditText.text?.toString() ?: "",
                nickname = viewBinding.nicknameEditText.text?.toString() ?: "",
                email = viewBinding.emailEditText.text?.toString() ?: "",
                password = viewBinding.passwordEditText.text?.toString() ?: ""
            )
        }
        viewBinding.termsAndConditionsCheckBox.setClubRulesText {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://policies.google.com/terms")
                )
            )
        }
        subscribeToFormFields()
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventsFlow.collect { event ->
                    when (event) {
                        is SignUpViewModel.Event.SignUpEmailConfirmationRequired -> {
                            findNavController().navigate(R.id.action_signUpFragment_to_emailConfirmationFragment)
                        }
                        else -> {
                            // nothing
                        }
                    }
                }
            }
        }
    }

    private fun onBackButtonPressed() {
        val firstname = viewBinding.firstnameEditText.text?.toString()
        val lastname = viewBinding.lastnameEditText.text?.toString()
        val nickname = viewBinding.nicknameEditText.text?.toString()
        val email = viewBinding.emailEditText.text?.toString()
        val password = viewBinding.passwordEditText.text?.toString()
        if (firstname.isNullOrBlank()
            && lastname.isNullOrBlank()
            && nickname.isNullOrBlank()
            && email.isNullOrBlank()
            && password.isNullOrBlank()
        ) {
            findNavController().popBackStack()
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.sign_in_back_alert_dialog_text)
            .setNegativeButton(R.string.sign_in_back_alert_dialog_no_text) { dialog, _ ->
                dialog?.dismiss()
            }
            .setPositiveButton(R.string.sign_in_back_alert_dialog_yes_text) { _, _ ->
                findNavController().popBackStack()
            }
            .show()
    }

    private fun subscribeToFormFields() {
        decideSignUpButtonEnabledState()

        viewBinding.firstnameEditText.doAfterTextChanged { firstname ->
            decideSignUpButtonEnabledState(
                firstname = firstname?.toString()
            )
        }
        viewBinding.lastnameEditText.doAfterTextChanged { lastname ->
            decideSignUpButtonEnabledState(
                lastname = lastname?.toString()
            )
        }
        viewBinding.nicknameEditText.doAfterTextChanged { nickname ->
            decideSignUpButtonEnabledState(
                nickname = nickname?.toString()
            )
        }
        viewBinding.emailEditText.doAfterTextChanged { email ->
            decideSignUpButtonEnabledState(
                email = email?.toString()
            )
        }
        viewBinding.passwordEditText.doAfterTextChanged { password ->
            decideSignUpButtonEnabledState(
                password = password?.toString()
            )
        }
        viewBinding.termsAndConditionsCheckBox.setOnCheckedChangeListener { _, isChecked ->
            decideSignUpButtonEnabledState(
                termsIsChecked = isChecked
            )
        }
    }

    private fun CheckBox.setClubRulesText(clubRulesClickListener: () -> Unit) {
        movementMethod = LinkMovementMethod.getInstance()

        val clubRulesClickSpan =
            object : ClickableSpan() {
                override fun onClick(widget: View) = clubRulesClickListener()
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = resources.getColor(R.color.purple_200, null)
                }
            }

        text =
            resources.getSpannedString(
                R.string.sign_up_terms_and_conditions_template,
                buildSpannedString {
                    inSpans(clubRulesClickSpan) {
                        append(resources.getString(R.string.sign_up_club_rules))
                    }
                }
            )
    }

    private fun decideSignUpButtonEnabledState(
        firstname: String? = null,
        lastname: String? = null,
        nickname: String? = null,
        email: String? = null,
        password: String? = null,
        termsIsChecked: Boolean? = null
    ) {
        val firstnameIn = firstname ?: viewBinding.firstnameEditText.text?.toString()
        val lastnameIn = lastname ?: viewBinding.lastnameEditText.text?.toString()
        val nicknameIn = nickname ?: viewBinding.nicknameEditText.text?.toString()
        val emailIn = email ?: viewBinding.emailEditText.text?.toString()
        val passwordIn = password ?: viewBinding.passwordEditText.text?.toString()
        val termsIsCheckedIn = termsIsChecked ?: viewBinding.termsAndConditionsCheckBox.isChecked

        viewBinding.signUpButton.isEnabled = !firstnameIn.isNullOrBlank()
                && !lastnameIn.isNullOrBlank()
                && !nicknameIn.isNullOrBlank()
                && !emailIn.isNullOrBlank()
                && !passwordIn.isNullOrBlank()
                && termsIsCheckedIn
    }

}