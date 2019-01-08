package ca.agoldfish.td_project_lazarus;

import android.support.design.widget.TextInputLayout;
import android.util.Patterns;

public class PhoneFieldValidator extends BaseValidator {
    public PhoneFieldValidator(TextInputLayout errorContainer) {
        super(errorContainer);
        mErrorMessage = "Invalid Phone Number";
        mEmptyMessage = "Missing Phone Number";
    }

    @Override
    protected boolean isValid(CharSequence charSequence) {
        return Patterns.PHONE.matcher(charSequence).matches();
    }
}