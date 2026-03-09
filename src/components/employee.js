import React, { useState, useEffect } from 'react';
import EmployeeService from '../Service/EmployeeService';

const HRContacts = () => {
    // --- 1. All Original State Hooks ---
    const [employees, setEmployees] = useState([]);
    const [employeeMap, setEmployeeMap] = useState({}); 
    const [selectedEmployees, setSelectedEmployees] = useState([]);
    const [searchTerm, setSearchTerm] = useState("");
    const [notification, setNotification] = useState({ message: '', type: '', visible: false });
    const [page, setPage] = useState(0);
    const [userEmail, setUserEmail] = useState("");
    const [gmailConnected, setGmailConnected] = useState(false);
    const [userId, setUserId] = useState(localStorage.getItem("gmailUserId") || "");
    const [lastLoadCount, setLastLoadCount] = useState(0);
    const [uploadingResume, setUploadingResume] = useState(false);
    const [frontResumeFile, setFrontResumeFile] = useState(null);
    const [frontResumeName, setFrontResumeName] = useState("");
    const [frontResumeServerName, setFrontResumeServerName] = useState("");
    
    const pageSize = 50;

    const [emailTemplate, setEmailTemplate] = useState({
        subject: "Interest in Career Opportunities at Your Company",
        body: `Dear [HR_NAME],\n\nI came across your profile at [COMPANY_NAME] and am very interested in potential opportunities at your organization.\n\nBest regards,\n[Your Name]`,
        attachResume: true
    });

    // --- 2. Notification Handler ---
    const showNotification = (message, type = 'success') => {
        setNotification({ message, type, visible: true });
        setTimeout(() => setNotification(prev => ({ ...prev, visible: false })), 3000);
    };

    // --- 3. Lifecycle & Auth ---
    useEffect(() => {
        const storedUserId = localStorage.getItem("gmailUserId");
        if(storedUserId){
            setUserId(storedUserId);
            setGmailConnected(true);
        } else {
            setGmailConnected(false);
        }
    }, []);

    useEffect(() => { getAllEmployees(); }, [page]);

    const getAllEmployees = () => {
        EmployeeService.getEmployee(page, pageSize).then((response) => {
            const list = response.data || [];
            setEmployees(list);
            setLastLoadCount(list.length);
            setEmployeeMap(prev => {
                const copy = { ...prev };
                list.forEach(emp => { copy[emp.id] = emp; });
                return copy;
            });
        }).catch(error => {
            console.log(error);
            showNotification('Failed to fetch HR.', 'error');
        });
    };

    const connectGmail = () => {
        if (!userEmail) { showNotification("⚠️ Please enter your Gmail first", "error"); return; }
        if(!userEmail.endsWith("@gmail.com")){ showNotification("Please enter a valid Gmail address","error"); return; }
        
        const clientId ="991112443742-e4mde8umo6j26btin7jpcokgv5r0huvc.apps.googleusercontent.com";
        const redirectUri = "https://automatecoldemail-bakend.onrender.com/users/oauth/callback";
        const scope = "https://www.googleapis.com/auth/gmail.send";

        const authUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=code&scope=${scope}&access_type=offline&prompt=consent`;
        window.location.href = authUrl;
    };

   const disconnectGmail = async () => {
    if (!userId) {
        showNotification("No active session found", "error");
        return;
    }

    try {
        // We use POST because we are triggering an action (Revocation) 
        // rather than just a simple data deletion.
        const response = await fetch(
            `https://automatecoldemail-bakend.onrender.com/users/disconnect/${userId}`, 
            { 
                method: "POST",
                headers: { 'Content-Type': 'application/json' }
            }
        );

        if (response.ok) {
            // Clear local state ONLY after the backend confirms Google revoked the token
            localStorage.removeItem("gmailUserId");
            setUserId("");
            setGmailConnected(false);
            setUserEmail("");
            showNotification("✅ Access fully revoked. Google will ask for permissions next time!");
        } else {
            showNotification("❌ Failed to revoke access from Google", "error");
        }
    } catch (err) {
        console.error("Revocation Error:", err);
        showNotification("❌ Network error during disconnection", "error");
    }
};
    // --- 4. Employee Management ---
    const deleteEmployee = (employeeId) => {
        EmployeeService.delete(employeeId).then(() => {
            setEmployeeMap(prev => {
                const copy = { ...prev };
                delete copy[employeeId];
                return copy;
            });
            getAllEmployees();
            setSelectedEmployees(selectedEmployees.filter(id => id !== employeeId));
            showNotification('HR deleted successfully.');
        }).catch(error => {
            console.log(error);
            showNotification('Failed to delete HR.', 'error');
        });
    };

    const deleteAllEmployees = () => {
        if (window.confirm('⚠️ Are you sure you want to delete ALL HR contacts?')) {
            const deletePromises = employees.map(emp => EmployeeService.delete(emp.id));
            Promise.all(deletePromises)
                .then(() => {
                    setSelectedEmployees([]);
                    setEmployeeMap({});
                    setPage(0);
                    getAllEmployees();
                    showNotification(`✅ All HR contacts deleted!`);
                })
                .catch(err => showNotification('❌ Error deleting contacts', 'error'));
        }
    };

    const searchEmployees = (e) => {
        e.preventDefault();
        if (searchTerm.trim()) {
            setPage(0);
            EmployeeService.searchEmployee(searchTerm).then((response) => {
                const list = response.data || [];
                setEmployees(list);
                setEmployeeMap(prev => {
                    const copy = { ...prev };
                    list.forEach(emp => { copy[emp.id] = emp; });
                    return copy;
                });
            }).catch(() => showNotification('Error searching HR.', 'error'));
        } else {
            getAllEmployees();
        }
    };

    // --- 5. Email Functionality ---
    const sendSingleEmail = async (employee) => {
        const emailRequest = {
            userId: userId,
            toEmail: employee.email,
            toName: employee.name,
            company: employee.company,
            subject: emailTemplate.subject.replace(/\[HR_NAME\]/g, employee.name).replace(/\[COMPANY_NAME\]/g, employee.company || ''),
            body: emailTemplate.body.replace(/\[HR_NAME\]/g, employee.name).replace(/\[COMPANY_NAME\]/g, employee.company || ''),
            attachResume: emailTemplate.attachResume,
            resumeFilename: emailTemplate.attachResume ? frontResumeServerName : undefined
        };
        try {
            const response = await fetch('https://automatecoldemail-bakend.onrender.com/api/email/send-single', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(emailRequest)
            });
            if (response.ok) showNotification(`✅ Email sent to ${employee.name}`);
            else showNotification(`❌ Failed to send to ${employee.name}`, 'error');
        } catch (error) { showNotification(`❌ Error sending email`, 'error'); }
    };

    const sendToSelected = async () => {
        let selectedContacts = selectedEmployees.map(id => employeeMap[id]).filter(Boolean);
        if (selectedContacts.length === 0) return showNotification('⚠️ No contacts loaded.', 'error');
        
        const emailRequests = selectedContacts.map(emp => ({
            userId: userId,
            toEmail: emp.email,
            toName: emp.name,
            company: emp.company,
            subject: emailTemplate.subject.replace('[HR_NAME]', emp.name).replace('[COMPANY_NAME]', emp.company || ''),
            body: emailTemplate.body.replace('[HR_NAME]', emp.name).replace('[COMPANY_NAME]', emp.company || ''),
            attachResume: emailTemplate.attachResume,
            resumeFilename: emailTemplate.attachResume ? frontResumeServerName : undefined
        }));

        try {
            const response = await fetch('https://automatecoldemail-bakend.onrender.com/api/email/send-bulk', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(emailRequests)
            });
            if (response.ok) {
                const report = await response.json();
                showNotification(`✅ Bulk send: ${report.successCount} success, ${report.failureCount} failed`);
            }
        } catch (error) { showNotification('❌ Error sending emails', 'error'); }
    };

    // --- 6. CSV & Resume Imports ---
    const importFromCSV = (e) => {
        const file = e.target.files[0];
        if (!file || !file.name.endsWith('.csv')) return showNotification('❌ Select a CSV', 'error');
        EmployeeService.importCSV(file).then((response) => {
            showNotification(`✅ Successfully imported ${response.data.importedCount} contacts!`);
            setPage(0); getAllEmployees();
            e.target.value = ''; 
        }).catch(error => showNotification(`❌ Import failed`, 'error'));
    };

    const exportToCSV = () => {
        window.location.href = "http://automatecoldemail-bakend.onrender.com/users/export";
    };

    // --- 7. Re-Engineered Styles ---
    const stunningCSS = `
        :root {
            --brand: #3d3887;
            --brand-dark: #9d98f9;
            --bg-page: #d7e9eb;
            --glass: rgba(253, 200, 239, 0.75);
        }

        .main-wrapper {
background: #833AB4;
background: linear-gradient(90deg, rgba(131, 58, 180, 1) 0%, rgba(253, 29, 29, 1) 50%, rgba(252, 176, 69, 1) 100%);            min-height: 100vh;
            padding: 3rem 1.5rem;
            font-family: 'Inter', sans-serif;
        }

        .container-box { max-width: 1200px; margin: 0 auto; }

        .glass-panel {
            background: var(--glass);
            backdrop-filter: blur(12px);
            border-radius: 20px;
            padding: 2rem;
            margin-bottom: 2rem;
            box-shadow: 0 10px 30px rgba(111, 18, 18, 0.15);
            border: 1px solid rgba(255,255,255,0.3);
        }

        .header-title { color: white; text-align: center; margin-bottom: 3rem; }
        .header-title h1 { font-size: 2.75rem; font-weight: 800; letter-spacing: -1px; text-shadow: 0 4px 10px rgba(0,0,0,0.2); }

        .form-row { display: flex; gap: 15px; flex-wrap: wrap; margin-bottom: 1.5rem; align-items: center; }
        
        .m-input {
            flex: 1; min-width: 250px;
            padding: 12px 18px;
            border-radius: 12px;
            border: 2px solid #dfe2e6;
            outline: none; transition: 0.3s;
        }
        .m-input:focus { border-color: var(--brand); box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1); }

        .btn-action {
            padding: 12px 24px;
            border-radius: 12px;
            font-weight: 700;
            border: none;
            cursor: pointer;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 8px;
        }

        .btn-primary { background: var(--brand); color: white; }
        .btn-primary:hover { background: var(--brand-dark); transform: translateY(-2px); }
        .btn-danger { background: #ff2323; color: white; }
        .btn-secondary { background: #f88d00; color: #fcfdff; border: 1px solid #e2e8f0; }

        .table-wrap { overflow-x: auto; margin-top: 1rem; border-radius: 15px; border: 1px solid #e2e8f0; }
        .custom-table { width: 100%; border-collapse: collapse; background: white; }
        .custom-table th { background: #f8fafc; padding: 1rem; color: #64748b; font-size: 0.85rem; text-transform: uppercase; text-align: left; }
        .custom-table td { padding: 1rem; border-top: 1px solid #f1f5f9; font-size: 0.95rem; }

        .status-badge {
            background: #dcfce7; color: #166534; padding: 4px 12px; border-radius: 20px; font-weight: 600; font-size: 0.8rem;
        }

        .toast-msg {
            position: fixed; top: 2rem; right: 2rem; padding: 1rem 2rem; border-radius: 12px;
            color: white; font-weight: 600; z-index: 9999; box-shadow: 0 20px 40px rgba(0,0,0,0.2);
            animation: slideIn 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
        }
        @keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
    `;

    return (
        <div className="main-wrapper">
            <style>{stunningCSS}</style>

            {notification.visible && (
                <div className="toast-msg" style={{ background: notification.type === 'error' ? '#ef4444' : '#10b981' }}>
                    {notification.message}
                </div>
            )}

            <div className="container-box">
                <div className="header-title">
                    <h1>COLD EMAIL AUTOMATION</h1>
                    <p>Effortlessly connect with HRs at scale</p>
                </div>

                {/* --- Gmail Auth Panel --- */}
                <div className="glass-panel">
                    <div style={{ display:'flex', justifyContent:'space-between', marginBottom:'1.5rem' }}>
                        <h3 style={{ fontWeight: 1000 }}>📩 Gmail Connectivity</h3>
                        {gmailConnected && <span className="status-badge">System Linked</span>}
                    </div>
                    <div className="form-row">
                        <input 
                            type="email" className="m-input" placeholder="example@gmail.com" 
                            value={userEmail} onChange={(e) => setUserEmail(e.target.value)}
                        />
                        <button className="btn-action btn-primary" onClick={connectGmail} disabled={gmailConnected}>
                            🔐 {gmailConnected ? 'Connected' : 'Authorize Gmail'}
                        </button>
                        {gmailConnected && (
                            <button className="btn-action btn-secondary" onClick={disconnectGmail}>Disconnect</button>
                        )}
                    </div>
                </div>

                {/* --- Template Panel --- */}
                <div className="glass-panel">
                    <h3 style={{ marginBottom: '1.5rem', fontWeight: 800 }}>📧 Dynamic Template</h3>
                    <div className="form-row" style={{ flexDirection: 'column', alignItems: 'stretch' }}>
                        <label style={{ fontWeight: 600, fontSize: '0.9rem' }}>Subject Line</label>
                        <input 
                            className="m-input" value={emailTemplate.subject} 
                            onChange={(e) => setEmailTemplate({...emailTemplate, subject: e.target.value})}
                        />
                        
                        <label style={{ fontWeight: 600, fontSize: '0.9rem' }}>Email Content</label>
                        <textarea 
                            className="m-input" rows="8" value={emailTemplate.body}
                            onChange={(e) => setEmailTemplate({...emailTemplate, body: e.target.value})}
                        />
                    </div>
                    <div className="form-row">
                        <div style={{ display:'flex', alignItems:'center', gap: '8px' }}>
                            <input 
                                type="checkbox" id="attach" checked={emailTemplate.attachResume}
                                onChange={(e) => setEmailTemplate({...emailTemplate, attachResume: e.target.checked})}
                            />
                            <label htmlFor="attach" style={{ cursor:'pointer' }}>📎 Include Resume Link</label>
                        </div>
                        
                        {emailTemplate.attachResume && (
                            <label className="btn-action btn-primary" style={{ margin: 0 }}>
                                {uploadingResume ? 'Uploading...' : '📤 Upload PDF/Doc'}
                                <input 
                                    type="file" style={{ display: 'none' }} 
                                    onChange={async (e) => {
                                        const file = e.target.files[0];
                                        if (!file) return;
                                        setUploadingResume(true);
                                        try {
                                            const resp = await EmployeeService.uploadResumeToServer(file);
                                            setFrontResumeName(file.name);
                                            setFrontResumeServerName(resp.data?.filename || '');
                                            showNotification('✅ Resume Ready');
                                        } catch (err) { showNotification('❌ Upload Failed', 'error'); }
                                        finally { setUploadingResume(false); }
                                    }}
                                />
                            </label>
                        )}
                        {frontResumeName && <span style={{ color: '#6366f1', fontWeight: 700 }}>{frontResumeName}</span>}
                    </div>
                </div>

                {/* --- Table & Actions Panel --- */}
                <div className="glass-panel">
                    <div className="form-row" style={{ justifyContent: 'space-between' }}>
                        <form onSubmit={searchEmployees} style={{ display:'flex', flex: 1, gap: '10px' }}>
                            <input 
                                className="m-input" placeholder="🔍 Search HR by name, email or company..." 
                                value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)}
                            />
                            <button type="submit" className="btn-action btn-primary">Search</button>
                        </form>
                        <div className="form-row" style={{ marginBottom: 0 }}>
                            <a href="/addemployee" className="btn-action btn-secondary">➕ Add Contact</a>
                            <button className="btn-action btn-primary" onClick={sendToSelected} disabled={selectedEmployees.length === 0}>
                                📤 Bulk Send ({selectedEmployees.length})
                            </button>
                        </div>
                    </div>

                    <div className="form-row" style={{ marginTop: '1.5rem', background: '#f8fafc', padding: '1rem', borderRadius: '12px' }}>
                        <button className="btn-action btn-secondary" onClick={exportToCSV}>📊 Export</button>
                        <label className="btn-action btn-secondary">
                            📥 Import CSV
                            <input type="file" style={{ display: 'none' }} onChange={importFromCSV} />
                        </label>
                        <button className="btn-action btn-danger" onClick={deleteAllEmployees}>🗑️ Delete All</button>
                    </div>

                    <div className="table-wrap">
                        <table className="custom-table">
                            <thead>
                                <tr>
                                    <th><input type="checkbox" onChange={(e) => setSelectedEmployees(e.target.checked ? employees.map(emp => emp.id) : [])} /></th>
                                    <th>HR Lead</th>
                                    <th>Email</th>
                                    <th>Company</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {employees.map(emp => (
                                    <tr key={emp.id}>
                                        <td>
                                            <input 
                                                type="checkbox" checked={selectedEmployees.includes(emp.id)}
                                                onChange={(e) => e.target.checked ? setSelectedEmployees([...selectedEmployees, emp.id]) : setSelectedEmployees(selectedEmployees.filter(id => id !== emp.id))}
                                            />
                                        </td>
                                        <td style={{ fontWeight: 700 }}>{emp.name}</td>
                                        <td style={{ color: '#64748b' }}>{emp.email}</td>
                                        <td><span style={{ background: '#f1f5f9', padding: '4px 8px', borderRadius: '6px' }}>{emp.company}</span></td>
                                        <td>
                                            <div style={{ display:'flex', gap: '8px' }}>
                                                <button className="btn-action btn-primary" style={{ padding: '6px 10px' }} onClick={() => sendSingleEmail(emp)}>⚡ Send</button>
                                                <button className="btn-action btn-danger" style={{ padding: '6px 10px' }} onClick={() => deleteEmployee(emp.id)}>🗑️</button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>

                    <div style={{ marginTop: '1.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <p style={{ color: '#64748b', fontSize: '0.9rem' }}>Page {page + 1}</p>
                        <div style={{ display: 'flex', gap: '10px' }}>
                            <button className="btn-action btn-secondary" onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}>◀ Prev</button>
                            <button className="btn-action btn-secondary" onClick={() => setPage(p => p + 1)} disabled={lastLoadCount < pageSize}>Next ▶</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default HRContacts;