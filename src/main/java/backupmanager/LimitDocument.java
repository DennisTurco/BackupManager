package backupmanager;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class LimitDocument extends PlainDocument {
    private int limit;

    public LimitDocument(int limit) {
        this.limit = limit;
    }

    @Override
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str == null) return;

        // check if the lenght does't exeed the limit
        if ((getLength() + str.length()) <= limit) {
            super.insertString(offset, str, attr);
        }
    }
}