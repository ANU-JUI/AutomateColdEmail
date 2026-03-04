import axios from 'axios';

const BASE_URL = "https://automatecoldemail-bakend.onrender.com/users";

class EmployeeService {
    save(employee, resumeFile) {
        const formData = new FormData();
        if (employee.id) {
            formData.append('id', employee.id);
        }
        formData.append('name', employee.name);
        formData.append('email', employee.email);
        formData.append('company', employee.company);
        if (resumeFile) {
            formData.append('resume', resumeFile);
        }
        
        return axios.post(`${BASE_URL}/create`, formData,
            { headers: { 'Content-Type': 'multipart/form-data' } }
        );
    }
    
    update(employee, resumeFile) {
        const formData = new FormData();
        // id must be present for update
        formData.append('id', employee.id);
        formData.append('name', employee.name);
        formData.append('email', employee.email);
        formData.append('company', employee.company);
        if (resumeFile) {
            formData.append('resume', resumeFile);
        }
        
        return axios.put(`${BASE_URL}/update/` + employee.id, formData,
            { headers: { 'Content-Type': 'multipart/form-data' } }
        );
    }
    
    delete(id) {
        return axios.delete(`${BASE_URL}/delete/` + id);
    }
    
    getEmployee() {
        return axios.get(`${BASE_URL}/get`);
    }
    
    searchEmployee(keyword) {
        return axios.get(`${BASE_URL}/search/` + keyword);
    }
    
    getEmployeeById(id) {
        // endpoint was updated to return user at /users/{id}
        return axios.get(`${BASE_URL}/` + id);
    }
    
    downloadResume(id) {
        return axios.get(`${BASE_URL}/download-resume/` + id, {
            responseType: 'blob'
        });
    }

    importCSV(csvFile) {
        const formData = new FormData();
        formData.append('file', csvFile);
        
        return axios.post(`${BASE_URL}/import-csv`, formData,
            { headers: { 'Content-Type': 'multipart/form-data' } }
        );
    }

    uploadResumeToServer(resumeFile) {
        const formData = new FormData();
        formData.append('resume', resumeFile);
        // endpoint is in EmailController
        return axios.post(`https://automatecoldemail-bakend.onrender.com/api/email/upload-resume`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
    }
}

export default new EmployeeService();
