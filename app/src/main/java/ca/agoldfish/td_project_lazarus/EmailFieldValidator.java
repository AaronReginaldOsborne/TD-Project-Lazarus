package ca.agoldfish.td_project_lazarus;

import android.support.design.widget.TextInputLayout;
import android.util.Patterns;

public class EmailFieldValidator extends BaseValidator {
    public EmailFieldValidator(TextInputLayout errorContainer) {
        super(errorContainer);
        mErrorMessage = "Invalid Email Address";
        mEmptyMessage = "Missing Email Address";
    }

    @Override
    protected boolean isValid(CharSequence charSequence) {
        return Patterns.EMAIL_ADDRESS.matcher(charSequence).matches();
    }
}