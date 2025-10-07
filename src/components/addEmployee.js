import React, { useState } from 'react'
import {useNavigate} from 'react-router-dom';
import EmployeeService from '../Service/EmployeeService';
const AddEmployee = () => {
    const [employee, setEmployee] = useState({ name: "", email: "", password: "", id: "" });
    const navigate = useNavigate();

    const handleChange = (e) => {
        const { name, value } = e.target;
        setEmployee(prev => ({ ...prev, [name]: value }));
    };
    
    const handleSave = (e) => {
        e.preventDefault();
        EmployeeService.save(employee)
            .then(() => {
                navigate("/");
            })
            .catch(error => {
                console.error("Error saving employee:", error);
                // You could add a user notification here
            });
    };
    
    const handleClear = () => {
        setEmployee({ name: "", email: "", password: "", id: "" });
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
                        <div className="form-group">
                            <label className="form-label">ID</label>
                            <input type="text" name="id" value={employee.id} placeholder="Enter ID" className="form-input" onChange={handleChange} />
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
                            <label className="form-label">Password</label>
                            <input type="password" name="password" value={employee.password} placeholder="Enter password" className="form-input" onChange={handleChange} />
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