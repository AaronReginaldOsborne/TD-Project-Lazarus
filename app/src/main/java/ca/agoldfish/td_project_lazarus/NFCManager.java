package ca.agoldfish.td_project_lazarus;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;

import java.io.IOException;

/**
 * Created by johnf on 10/4/2016.
 */
public class NFCManager {

    //region Properties

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    Activity activity;

    TagReadListener onTagReadListener;
    TagWriteListener onTagWriteListener;
    TagWriteErrorListener onTagWriteErrorListener;

    String writeData = null;

    //endregion

    //region Constructors

    public NFCManager(Activity activity) {
        this.activity = activity;
    }

    //endregion

    //region setTagListeners

    /**
     * sets the listener to read events
     *
     * @param onTagReadListener
     */
    public void setOnTagReadListener(TagReadListener onTagReadListener) {
        this.onTagReadListener = onTagReadListener;
    }

    /**
     * sets the listener to write events
     *
     * @param onTagWriteListener
     */
    public void setOnTagWriteListener(TagWriteListener onTagWriteListener) {
        this.onTagWriteListener = onTagWriteListener;
    }

    /**
     * sets the listener to write error events
     *
     * @param onTagWriteErrorListener
     */
    public void setOnTagWriteErrorListener(TagWriteErrorListener onTagWriteErrorListener) {
        this.onTagWriteErrorListener = onTagWriteErrorListener;
    }

    //endregion

    //region writeData

    /**
     * Indicates that we want to write teh given data to the next tag detected
     *
     * @param writeData
     */
    public void setWrittenData(String writeData) {
        this.writeData = writeData;
    }

    public String getWrittenData() {
        return this.writeData;
    }

    /**
     * Stops a writeData operation
     */
    private void clearWriteData() {
        this.writeData = null;
    }

    // endregion


    /**
     * To be executed on OnCreate of activity
     *
     * @return true if the device has nfc capabilities
     */
    public boolean onActivityCreate() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
        pendingIntent = PendingIntent.getActivity(activity, 0,
                new Intent(activity, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        return nfcAdapter != null;
    }

    /**
     * To be executed on onResume of the activity
     */
    public void onActivityResume() {
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled()) {
                //TODO indicate that wireless should be opened
            }
            nfcAdapter.enableForegroundDispatch(activity, pendingIntent, null, null);
        }
    }

    /**
     * To be executed onResume of activity
     */
    public void onActivityPause() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(activity);
        }
    }

    /**
     * Reads a tag for given intent and notifies listeners
     *
     * @param intent
     */
    public void readTagFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag myTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                NdefRecord[] records = ((NdefMessage) rawMsgs[0]).getRecords();
                String data = ndefRecordToString(records[0]);
                //TODO look at how this works
                onTagReadListener.onTagRead(data);
            }
        }
    }

    public String ndefRecordToString(NdefRecord record) {
        byte[] payload = record.getPayload();
        return new String(payload);
    }

    public void clearTag(Intent intent) {
        try {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            writeToTag(tag, new NdefMessage(new NdefRecord(NdefRecord.TNF_EMPTY, null, null, null)));

        } catch (NFCWriteException exception) {
            onTagWriteErrorListener.onTagWriteError(exception);
        }
    }

    public void writeDataToTag(Intent intent, String data) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        try {
            //Record with actual data wee care about
            NdefRecord relayRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, null, data.getBytes());

            //Complete NDEF message with both records
            NdefMessage message = new NdefMessage(new NdefRecord[]{relayRecord});
            setWrittenData(data);
            writeToTag(tag, message);
        } catch (NFCWriteException exception) {
            onTagWriteErrorListener.onTagWriteError(exception);
        } finally {
            clearWriteData();
        }
    }

    private void writeToTag(Tag tag, NdefMessage message) throws NFCWriteException {
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            //If the tag is already formatted, just write the message to it
            try {
                ndef.connect();
            } catch (IOException e) {
                throw new NFCWriteException(NFCWriteException.NFCErrorType.unknownError);
            }
            // Make sure the tag is writable
            if (!ndef.isWritable()) {
                throw new NFCWriteException(NFCWriteException.NFCErrorType.ReadOnly);
            }

            // Check if there's enough space on the tag for the message
            int size = message.toByteArray().length;
            if (ndef.getMaxSize() < size) {
                throw new NFCWriteException(NFCWriteException.NFCErrorType.NoEnoughSpace);
            }

            try {
                ndef.writeNdefMessage(message);
            } catch (TagLostException tle) {
                throw new NFCWriteException(NFCWriteException.NFCErrorType.tagLost, tle);
            } catch (IOException ioe) {
                throw new NFCWriteException(NFCWriteException.NFCErrorType.formattingError, ioe);
            } catch (FormatException fe) {
                throw new NFCWriteException(NFCWriteException.NFCErrorType.formattingError, fe);
            }
        } else {
            NdefFormatable format = NdefFormatable.get(tag);
            if (format != null) {
                try {
                    format.connect();
                    format.format(message);
                } catch (TagLostException tle) {
                    throw new NFCWriteException(NFCWriteException.NFCErrorType.tagLost, tle);
                } catch (IOException ioe) {
                    throw new NFCWriteException(NFCWriteException.NFCErrorType.formattingError, ioe);
                } catch (FormatException fe) {
                    throw new NFCWriteException(NFCWriteException.NFCErrorType.formattingError, fe);
                }
            } else {
                throw new NFCWriteException(NFCWriteException.NFCErrorType.noNdefError);
            }
        }
        onTagWriteListener.onTagWritten();
    }

    public void readTag(Intent intent) {
        readTagFromIntent(intent);
    }


    public interface TagReadListener {
        public void onTagRead(String tagRead);
    }

    public interface TagWriteListener {
        public void onTagWritten();
    }

    public interface TagWriteErrorListener {
        public void onTagWriteError(NFCWriteException exception);
    }
}

