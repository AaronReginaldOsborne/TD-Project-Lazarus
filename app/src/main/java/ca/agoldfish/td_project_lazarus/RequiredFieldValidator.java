package ca.agoldfish.td_project_lazarus;

import android.support.design.widget.TextInputLayout;
import android.util.Patterns;

public class RequiredFieldValidator extends BaseValidator {
    public RequiredFieldValidator(TextInputLayout errorContainer) {
        super(errorContainer);
        mEmptyMessage = "This Field is required";
    }

    @Override
    protected boolean isValid(CharSequence charSequence) {
        return charSequence != null && charSequence.length() > 0;
    }
}