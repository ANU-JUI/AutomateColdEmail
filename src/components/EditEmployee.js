import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import EmployeeService from '../Service/EmployeeService';
// --- Employee Service Logic ---
// API endpoints are configured to match the specific paths your backend requires.

const EditEmployee = () => {
    // The useParams hook extracts the 'id' from the URL (e.g., /editemployee/123)
    const { id } = useParams();
    const navigate = useNavigate();
    
    // State for the form, loading status, and any potential errors
    const [employee, setEmployee] = useState({ name: "", email: "", company: "", id: id });
    const [resume, setResume] = useState(null);
    const [resumeName, setResumeName] = useState("");
    const [originalEmployee, setOriginalEmployee] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // This useEffect hook runs when the component mounts and whenever the 'id' changes.
    useEffect(() => {
        setLoading(true);
        setError(null);
        // fetch existing employee data from backend
        EmployeeService.getEmployeeById(id)
            .then(resp => {
                const data = resp.data || {};
                setEmployee({
                    id: data.id,
                    name: data.name || "",
                    email: data.email || "",
                    company: data.company || ""
                });
                setOriginalEmployee({
                    id: data.id,
                    name: data.name || "",
                    email: data.email || "",
                    company: data.company || ""
                });
            })
            .catch(err => {
                console.error("Failed to fetch employee details", err);
            })
            .finally(() => {
                setLoading(false);
            });
    }, [id, navigate]);
    
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

    //const data=EmployeeService.getEmployeeById(id);
    const handleUpdate = (e) => {
        e.preventDefault();
        const payload = { ...employee };
        // we don't handle passwords anymore
        delete payload.password;

        EmployeeService.update(payload, resume)
            .then(() => {
                navigate("/");
            })
            .catch(error => {
                console.error("Error updating employee:", error);
                setError("Failed to update employee. Please try again.");
            });
    };
    
    const handleClear = () => {
        setEmployee(originalEmployee || { name: "", email: "", company: "", id: "" });
        setResume(null);
        setResumeName("");
    };

    const styles = `
        .form-container, .status-container {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 2rem;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            color: white;
        }
        .form-card, .status-card {
            background: white;
            padding: 2.5rem;
            border-radius: 15px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            width: 100%;
            max-width: 500px;
            animation: fadeIn 0.5s ease-out;
            color: #2d3748;
        }
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(-20px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .form-header {
            text-align: center;
            margin-bottom: 2rem;
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
        .form-input:disabled {
            background-color: #f7fafc;
            color: #a0aec0;
            cursor: not-allowed;
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
            border: 2px solid #e2e8f0;
        }
        .btn-clear:hover {
            background-color: #e2e8f0;
        }
        .btn-cancel, .btn-back {
            background: linear-gradient(135deg, #f56565, #e53e3e);
            color: white;
        }
        .btn-cancel:hover, .btn-back:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(229, 62, 62, 0.4);
        }
        .status-card {
            text-align: center;
            color: #e53e3e;
        }
        .status-card p {
             margin: 1.5rem 0;
             color: #4a5568;
             line-height: 1.6;
        }
    `;

    if (loading) {
        return <div className="status-container"><h2>Loading Employee Data...</h2></div>;
    }

    if (error) {
        return (
            <div className="status-container">
                <div className="status-card">
                    <h2>Something Went Wrong</h2>
                    <p>{error}</p>
                    <button className="btn btn-back" onClick={() => navigate("/")}>Go Back</button>
                </div>
            </div>
        );
    }
    
    return (
        <>
            <style>{styles}</style>
            <div className="form-container">
                <div className="form-card">
                    <h1 className="form-header">EDIT HR 🧑‍💼</h1>
                    <form onSubmit={handleUpdate}>
                        <div className="form-group">
                            <label className="form-label">ID</label>
                            <input type="text" name="id" value={employee.id} className="form-input" disabled />
                        </div>
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
                            <input type="text" name="company" value={employee.company} placeholder="Enter company" className="form-input" onChange={handleChange} />
                        </div>
                        {/* Resume upload moved to main page; editing does not upload resumes */}
                        <div className="form-actions">
                            <button type="submit" className="btn btn-save">Update</button>
                            <button type="button" className="btn btn-clear" onClick={handleClear}>Reset</button>
                            <button type="button" className="btn btn-cancel" onClick={() => navigate("/")}>Cancel</button>
                        </div>
                    </form>
                </div>
            </div>
        </>
    );
};

export default EditEmployee;
