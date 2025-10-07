import axios from 'axios'
const BASE_URL="http://localhost:9000/users"
class EmployeeService

{
    save(employee)
    {
        return axios.post(`${BASE_URL}/create`,JSON.stringify(employee),
         { headers: { 'Content-Type': 'application/json' }}
    );      
} 
update(employee)
{
    return axios.put(`${BASE_URL}/update/`+employee.id,JSON.stringify(employee),
    { headers: { 'Content-Type': 'application/json' }}
);  
}
delete(id)
{
    return axios.delete(`${BASE_URL}/delete/`+id);
}
getEmployee()
{
    return axios.get(`${BASE_URL}/get`);
}
searchEmployee(keyword)
{
    return axios.get(`${BASE_URL}/search/`+keyword);
}
getEmployeeById(id)
{
    return axios.get(`${BASE_URL}/get/`+id);
}
}
export default new EmployeeService();