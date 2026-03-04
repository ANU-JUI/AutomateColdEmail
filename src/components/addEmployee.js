import React, { useState } from 'react'
import {useNavigate} from 'react-router-dom';
import EmployeeService from '../Service/EmployeeService';
const AddEmployee = () => {
    const [employee, setEmployee] = useState({ name: "", email: "", company: "" });
    const [resume, setResume] = useState(null);
    const [resumeName, setResumeName] = useState("");
    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setEmployee(prev => ({ ...prev, [name]: value }));
    };

    const handleResumeChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setResume(file);
            setResumeName(file.name);
        }
    };
    
    const handleSave = (e) => {
        e.preventDefault();
        if (!employee.name || !employee.email || !employee.company) {
            alert("Please fill in all required fields");
            return;
        }
        if (!resume) {
            alert("Please upload a resume");
            return;
        }
        EmployeeService.save(employee, resume)
            .then(() => {
                navigate("/");
            })
            .catch(error => {
                console.error("Error saving employee:", error);
                alert("Error saving employee: " + (error.response?.data?.message || error.message));
            });
    };
    
    const handleClear = () => {
        setEmployee({ name: "", email: "", company: "" });
        setResume(null);
        setResumeName("");
    };

    const styles = `
        .form-container {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 2rem;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }
        .form-card {
            background: white;
            padding: 2.5rem;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            width: 100%;
            max-width: 500px;
            animation: fadeIn 0.5s ease-out;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .form-header {
            text-align: center;
            margin-bottom: 2rem;
            color: #2d3748;
            font-size: 1.8rem;
            font-weight: 700;
        }
        .form-group {
            margin-bottom: 1.5rem;
        }
        .form-label {
            display: block;
            margin-bottom: 0.5rem;
            color: #4a5568;
            font-weight: 600;
        }
        .form-input {
            width: 100%;
            padding: 0.75rem 1rem;
            border: 2px solid #e2e8f0;
            border-radius: 8px;
            font-size: 1rem;
            transition: all 0.3s ease;
        }
        .form-input:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
        }
        .file-input-wrapper {
            position: relative;
            overflow: hidden;
            display: inline-block;
            width: 100%;
        }
        .file-input-label {
            display: block;
            margin-bottom: 0.5rem;
            color: #4a5568;
            font-weight: 600;
        }
        .file-input-button {
            display: flex;
            align-items: center;
            justify-content: center;
            width: 100%;
            padding: 0.75rem 1rem;
            background: #f7fafc;
            border: 2px dashed #cbd5e0;
            border-radius: 8px;
            cursor: pointer;
            font-size: 0.95rem;
            color: #4a5568;
            transition: all 0.3s ease;
        }
        .file-input-button:hover {
            background: #edf2f7;
            border-color: #667eea;
        }
        .file-input-hidden {
            display: none;
        }
        .resume-selected {
            color: #48bb78;
            font-weight: 600;
            margin-top: 0.5rem;
        }
        .form-actions {
            display: flex;
            justify-content: space-between;
            gap: 1rem;
            margin-top: 2rem;
        }
        .btn {
            padding: 0.75rem 1.5rem;
            border: none;
            border-radius: 8px;
            font-size: 1rem;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            flex-grow: 1;
        }
        .btn-save {
            background: linear-gradient(135deg, #48bb78, #38a169);
            color: white;
        }
        .btn-save:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(56, 161, 105, 0.4);
        }
        .btn-clear {
            background-color: #f7fafc;
            color: #4a5568;
            border: 2px solid #e2e8f0;group">
                            <label className="file-input-label">Resume</label>
                            <div className="file-input-wrapper">
                                <label htmlFor="resume-input" className="file-input-button">
                                    📄 Click to upload or drag and drop
                                </label>
                                <input
                                    id="resume-input"
                                    type="file"
                                    name="resume"
                                    accept=".pdf,.doc,.docx"
                                    className="file-input-hidden"
                                    onChange={handleResumeChange}
                                />
                            </div>
                            {resumeName && <div className="resume-selected">✓ {resumeName}</div>}
                        </div>
                        <div className="form-
        }
        .btn-clear:hover {
            background-color: #e2e8f0;
        }
        .btn-cancel {
            background: linear-gradient(135deg, #f56565, #e53e3e);
            color: white;
        }
        .btn-cancel:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(229, 62, 62, 0.4);
        }
    `;

    return (
        <>
            <style>{styles}</style>
            <div className="form-container">
                <div className="form-card">
                    <h1 className="form-header">ADD HR 🧑‍💼</h1>
                    <form onSubmit={handleSave}>
                        {/* ID is auto-generated by the backend, no need for user input */}
                        <div className="form-group">
                            <label className="form-label">Name</label>
                            <input type="text" name="name" value={employee.name} placeholder="Enter full name" className="form-input" onChange={handleChange} />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Email</label>
                            <input type="email" name="email" value={employee.email} placeholder="Enter email address" className="form-input" onChange={handleChange} />
                        </div>
                        <div className="form-group">
                            <label className="form-label">Company</label>
                            <input type="text" name="company" value={employee.company} placeholder="Enter company name" className="form-input" onChange={handleChange} />
                        </div>
                        <div className="form-actions">
                            <button type="submit" className="btn btn-save">Save</button>
                            <button type="button" className="btn btn-clear" onClick={handleClear}>Clear</button>
                            <button type="button" className="btn btn-cancel" onClick={() => navigate("/")}>Cancel</button>
                        </div>
                    </form>
                </div>
            </div>
        </>
    );
};

export default AddEmployee;