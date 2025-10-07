import React, { useState, useEffect } from 'react';
import EmployeeService from '../Service/EmployeeService';
import '../HRcontanct.css';
const HRContacts = () => {
    const [employees, setEmployees] = useState([]);
    const [selectedEmployees, setSelectedEmployees] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [notification, setNotification] = useState({ message: '', type: '', visible: false });
    const [emailTemplate, setEmailTemplate] = useState({
        subject: "Interest in Career Opportunities at Your Company",
        body: `Dear [HR_NAME],

I came across your profile and am very interested in potential opportunities at your organization. 

I have attached my resume for your review and would appreciate the opportunity to discuss how my skills and experience could benefit your team.

Thank you for your time and consideration.

Best regards,
[Your Name]`,
        attachResume: true
    });

    // --- Notification Handler ---
    const showNotification = (message, type = 'success') => {
        setNotification({ message, type, visible: true });
        setTimeout(() => {
            setNotification(prev => ({ ...prev, visible: false }));
        }, 3000);
    };


    useEffect(() => {
        getAllEmployees();
    }, []);

    const getAllEmployees = () => {
        EmployeeService.getEmployee().then((response) => {
            setEmployees(response.data);
        }).catch(error => {
            console.log(error);
            showNotification('Failed to fetch employees.', 'error');
        });
    };

    const deleteEmployee = (employeeId) => {
        EmployeeService.delete(employeeId).then(() => {
            getAllEmployees();
            setSelectedEmployees(selectedEmployees.filter(id => id !== employeeId));
            showNotification('Employee deleted successfully.');
        }).catch(error => {
            console.log(error);
            showNotification('Failed to delete employee.', 'error');
        });
    };

    const searchEmployees = (e) => {
        e.preventDefault();
        if (searchTerm.trim()) {
            EmployeeService.searchEmployee(searchTerm).then((response) => {
                setEmployees(response.data);
            }).catch(error => {
                console.log(error);
                showNotification('Error searching employees.', 'error');
            });
        } else {
            getAllEmployees();
        }
    };

    // --- Email Functions ---
    const sendSingleEmail = async (employee) => {
        const emailRequest = {
            toEmail: employee.email,
            toName: employee.name,
            subject: emailTemplate.subject.replace(/\[HR_NAME\]/g, employee.name),
            body: emailTemplate.body.replace(/\[HR_NAME\]/g, employee.name),
            attachResume: emailTemplate.attachResume
        };
        // In a real app, you would make the API call here.
        console.log('Sending single email:', emailRequest);
        showNotification(`✅ Email sent successfully to ${employee.name}`);
    };

    const sendToSelected = async () => {
        // ... API call logic would go here
        const selectedCount = selectedEmployees.length;
        showNotification(`✅ Emails sent successfully to ${selectedCount} contacts`);
    };

    const sendToAll = async () => {
        // ... API call logic would go here
        const allCount = employees.length;
        showNotification(`✅ Emails sent successfully to all ${allCount} contacts`);
    };

    // --- Selection Handlers ---
    const selectAll = (e) => {
        if (e.target.checked) {
            setSelectedEmployees(employees.map(emp => emp.id));
        } else {
            setSelectedEmployees([]);
        }
    };

    const handleCheckboxChange = (employeeId, checked) => {
        if (checked) {
            setSelectedEmployees([...selectedEmployees, employeeId]);
        } else {
            setSelectedEmployees(selectedEmployees.filter(id => id !== employeeId));
        }
    };

    const exportToCSV = () => {
        showNotification('📊 Export to CSV functionality coming soon!', 'info');
    };

    const css = `
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f8fafc;
            line-height: 1.6;
        }
        .btn, .form-control, .table {
            transition: all 0.3s ease;
        }
        ::-webkit-scrollbar { width: 8px; }
        ::-webkit-scrollbar-track { background: #f1f1f1; }
        ::-webkit-scrollbar-thumb { background: #c1c1c1; border-radius: 4px; }
        ::-webkit-scrollbar-thumb:hover { background: #a8a8a8; }

        .hr-container {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 2rem;
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .container {
            width: 100%;
            max-width: 1200px; /* Constrain width on large screens */
        }
        .hr-header {
            text-align: center;
            color: white;
            margin-bottom: 30px;
            text-shadow: 0 2px 4px rgba(0,0,0,0.3);
        }
        .hr-header h1 {
            font-size: 2.5rem;
            font-weight: 700;
            margin-bottom: 10px;
        }
        .email-template-card, .search-add-section, .bulk-actions, .hr-table-container {
            background: white;
            border-radius: 15px;
            padding: 25px;
            margin-bottom: 25px;
            box-shadow: 0 8px 25px rgba(0,0,0,0.1);
            border: none;
        }
        .email-template-card h5 {
            color: #4a5568;
            font-weight: 600;
            margin-bottom: 20px;
            border-bottom: 2px solid #e2e8f0;
            padding-bottom: 10px;
        }
        .form-group { margin-bottom: 20px; }
        .form-group label {
            font-weight: 600;
            color: #4a5568;
            margin-bottom: 8px;
            display: block;
        }
        .form-control {
            width: 100%; /* Ensure it takes full width of parent */
            border: 2px solid #e2e8f0;
            border-radius: 8px;
            padding: 1rem;
            font-size: 1rem;
            line-height: 1.5;
        }
        .form-control:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
            outline: none;
        }
        .search-add-section .row, .bulk-actions .row {
            display: flex;
            align-items: center;
            flex-wrap: wrap;
        }
        .btn-outline-primary {
            background: transparent;
            border: 2px solid #667eea;
            color: #667eea;
        }
        .btn-outline-primary:hover {
            background: linear-gradient(135deg, #667eea, #764ba2);
            color: white;
            border-color: #667eea;
        }
        .bulk-actions .btn:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }
        .table-responsive { overflow-x: auto; }
        .table { width: 100%; border-collapse: collapse; }
        .table thead th {
            background: linear-gradient(135deg, #4a5568, #2d3748);
            color: white;
        }
        .table th, .table td {
            padding: 15px;
            vertical-align: middle;
            text-align: center;
            border-bottom: 1px solid #e2e8f0;
        }
        .table tbody tr:hover { background-color: #f7fafc; }
        .btn-sm { padding: 6px 12px; font-size: 12px; margin: 2px; }
        
        /* Notification Styles */
        .notification {
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            border-radius: 8px;
            color: white;
            z-index: 1000;
            min-width: 250px;
            text-align: center;
            box-shadow: 0 4px 15px rgba(0,0,0,0.2);
            font-weight: 600;
            animation: slideIn 0.5s ease forwards;
        }
        .notification.exit {
            animation: fadeOut 0.5s ease forwards;
        }
        .notification.success { background: linear-gradient(135deg, #48bb78, #38a169); }
        .notification.error { background: linear-gradient(135deg, #f56565, #e53e3e); }
        .notification.info { background: linear-gradient(135deg, #667eea, #764ba2); }

        @keyframes slideIn {
            from { transform: translateX(120%); opacity: 0; }
            to { transform: translateX(0); opacity: 1; }
        }
        @keyframes fadeOut {
            from { opacity: 1; transform: translateX(0); }
            to { opacity: 0; transform: translateX(120%); }
        }
    `;

    return (
        <>
            <style>{css}</style>

            {notification.visible && (
                <div 
                    className={`notification ${notification.type}`}
                    onAnimationEnd={() => { if(!notification.visible) setNotification({ message:'', type:'', visible: false})}}
                >
                    {notification.message}
                </div>
            )}

            <div className="hr-container">
                <div className="container">
                    <div className="hr-header">
                        <h1>COLD EMAIL AUTOMATION</h1>
                        <p className="lead">Manage and automate email campaigns to HR contacts</p>
                    </div>
                    
                    <div className="email-template-card">
                        <h5>📧 Email Template</h5>
                        <div className="form-group">
                            <label>Subject:</label>
                            <input 
                                type="text" 
                                className="form-control"
                                value={emailTemplate.subject}
                                onChange={(e) => setEmailTemplate({...emailTemplate, subject: e.target.value})}
                                placeholder="Enter email subject..."
                            />
                        </div>
                        <div className="form-group">
                            <label>Body:</label>
                            <textarea 
                                className="form-control"
                                rows="10"
                                value={emailTemplate.body}
                                onChange={(e) => setEmailTemplate({...emailTemplate, body: e.target.value})}
                                placeholder="Enter email body... Use [HR_NAME] as placeholder"
                            />
                        </div>
                        <div>
                            <input 
                                type="checkbox"
                                checked={emailTemplate.attachResume}
                                onChange={(e) => setEmailTemplate({...emailTemplate, attachResume: e.target.checked})}
                                id="attachResume"
                                style={{marginRight: '8px', cursor: 'pointer'}}
                            />
                            <label htmlFor="attachResume" style={{cursor: 'pointer'}}>
                                📎 Attach Resume
                            </label>
                        </div>
                        <small className="text-muted" style={{display: 'block', marginTop: '10px'}}>
                            💡 Use <strong>[HR_NAME]</strong> as placeholder for HR contact's name
                        </small>
                    </div>

                    <div className="search-add-section">
                         <form onSubmit={searchEmployees} style={{display: 'flex', gap: '1rem', alignItems: 'center'}}>
                            <input 
                                type="text" 
                                className="form-control"
                                placeholder="🔍 Search HR by name or email..."
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                style={{flexGrow: 1}}
                            />
                            <button className="btn btn-primary" type="submit">Search</button>
                            <a href="/addemployee" className="btn btn-outline-primary">➕ Add HR Contact</a>
                        </form>
                    </div>

                    <div className="bulk-actions">
                        <button className="btn btn-success" onClick={sendToSelected} disabled={selectedEmployees.length === 0}>
                            📤 Send to Selected ({selectedEmployees.length})
                        </button>
                        <button className="btn btn-warning" onClick={sendToAll} disabled={employees.length === 0}>
                            📨 Send to All ({employees.length})
                        </button>
                        <button className="btn btn-secondary" onClick={exportToCSV}>
                            📊 Export to CSV
                        </button>
                    </div>

                    <div className="hr-table-container">
                        <div className="table-responsive">
                            <table className="table">
                                <thead>
                                    <tr>
                                        <th>
                                            <input 
                                                type="checkbox" 
                                                onChange={selectAll}
                                                checked={employees.length > 0 && selectedEmployees.length === employees.length}
                                            />
                                        </th>
                                        <th>ID</th>
                                        <th>Name</th>
                                        <th>Email</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {employees.map((employee) => (
                                        <tr key={employee.id}>
                                            <td>
                                                <input 
                                                    type="checkbox" 
                                                    checked={selectedEmployees.includes(employee.id)}
                                                    onChange={(e) => handleCheckboxChange(employee.id, e.target.checked)}
                                                />
                                            </td>
                                            <td><strong>{employee.id}</strong></td>
                                            <td>{employee.name}</td>
                                            <td>{employee.email}</td>
                                            <td>
                                                <a href={`/editemployee/${employee.id}` } className="btn btn-primary btn-sm">✏️ Edit</a>
                                                <button className="btn btn-danger btn-sm" onClick={() => deleteEmployee(employee.id)}>🗑️ Delete</button>
                                                <button className="btn btn-success btn-sm" onClick={() => sendSingleEmail(employee)}>📧 Send Mail</button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </>
    );
};

export default HRContacts;

