# CSV Import Guide

## Overview
The CSV import feature allows you to bulk import HR contacts into the system from a CSV file.

## CSV File Format

Your CSV file should have the following structure:

```
ID,Name,Email,Password
emp001,John Doe,john.doe@company.com,SecurePass123
emp002,Jane Smith,jane.smith@company.com,SecurePass456
emp003,Robert Johnson,robert.j@company.com,SecurePass789
```

### Column Requirements:

1. **ID** (Required) - Unique identifier for the HR contact
   - alphanumeric
   - Must be unique across the system

2. **Name** (Required) - Full name of the HR contact
   - Can contain spaces and special characters

3. **Email** (Required) - Valid email address
   - Must follow email format: username@domain.com
   - Must be unique across the system

4. **Password** (Optional) - Password for the account
   - If not provided, system generates: `TempPassword@123`
   - Recommended to change on first login

## How to Import

1. Navigate to the HR Contacts page (main dashboard)
2. Click the **"📥 Import from CSV"** button in the bulk actions section
3. Select your CSV file from your device
4. The system will validate the file and import all valid records
5. You'll see a notification showing how many contacts were successfully imported

## CSV Format Examples

### Minimal Format (3 columns):
```csv
ID,Name,Email
HR001,Alice Brown,alice@example.com
HR002,Bob Wilson,bob@example.com
```

### Full Format (4 columns with passwords):
```csv
ID,Name,Email,Password
HR001,Alice Brown,alice@example.com,MySecurePass@123
HR002,Bob Wilson,bob@example.com,AnotherPass@456
```

## Important Notes

- **Header Row**: First row must contain column headers (ID, Name, Email, Password)
- **CSV Format**: File must be a valid CSV file with `.csv` extension
- **Validation**: Invalid rows are skipped, but valid rows will be imported
- **Email Validation**: System validates email format
- **Duplicates**: Users with duplicate IDs or emails may cause import failures
- **Quotes**: Fields with commas should be enclosed in quotes: `"ID, Special Name",Name1,email@example.com`
- **Character Encoding**: Use UTF-8 encoding for support of special characters

## Error Handling

If some records fail to import:
- Check the notification message for details
- Verify email addresses are in correct format
- Ensure IDs are unique
- Check that required fields (ID, Name, Email) are not empty
- Verify file is properly formatted as CSV

## Success Response

After successful import, you will see a message like:
```
✅ Successfully imported 10 HR contacts from CSV!
```

The imported contacts will immediately appear in the HR contacts list and you can send emails to them.

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "Please select a CSV file" | Ensure you selected a `.csv` file, not `.xlsx` or `.txt` |
| "Error importing CSV" | Check file format and ensure all rows have proper structure |
| Some contacts don't appear | Check the error logs - they may have had duplicate emails/IDs |
| Emails not valid | Ensure email addresses follow format: `name@domain.com` |

## Tips for Best Results

1. **Test First**: Start with a small test file (2-3 records) to verify format
2. **Backup**: Keep a backup of your original CSV file
3. **Validation**: Pre-validate your data before importing
4. **Unique IDs**: Ensure all ID values are unique in your CSV
5. **Unique Emails**: Ensure all email addresses are unique (no duplicates)
6. **Remove Old Data**: Consider archiving old contacts before bulk imports to avoid confusion
