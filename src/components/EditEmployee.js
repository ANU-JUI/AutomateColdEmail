import React,{useState} from 'react'
import EmployeeService from '../Service/EmployeeService';
import {useNavigate} from 'react-router-dom';

const EditEmployee = () => {
    const nav =useNavigate();
    const [employee,setEmployee]=useState({
      name:"",
      email:"",
      password:"",
      id:"",
    }
    );
    const employeeData = {
      name:String(employee.name), 
       email: String(employee.email), password: String(employee.password), id: String(employee.id),};
    const save= (e)=>
    {
      e.preventDefault();
      console.log("Employee data being sent:",employeeData);
      EmployeeService.update(employeeData)
      .then((response)=>
      {
  
        nav("/");
        console.log(response);
      })
      .catch((error)=>{
        console.log(error);
      }
    )
    }
    const handleChange =(e) =>
    {
      const  value =e.target.value;
      setEmployee({...employee,[e.target.name]:value})
    }
  return (
    <>
    <div class="max-w-xl mx-40 bg-red-200 my-20 rounded shadow py-4 px-8">
    <div class="text-4xl tracking-wider font-bold text-center py-4 px-8 ">
        <h1>EDIT EMPLOYEE 🧑‍💼</h1>
    </div>
    <div class="mx-10 my-2">
        <input type="text" name="id" placeholder='Id' value={employee.id} class="w-full py-2 my-5" onChange={(e)=>handleChange(e)}></input>
        <input type="text" name="name" placeholder='Name' value={employee.name} class="w-full py-2 my-5" onChange={(e)=>handleChange(e)}></input>
        <input type="text" name="email" placeholder='Email' value={employee.email} class="w-full py-2 my-5" onChange={(e)=>handleChange(e)}></input>
        <input type="text" name="password" placeholder='Password' value={employee.password} class="w-full py-2 my-5" onChange={(e)=>handleChange(e)}></input>
    </div>
    <div class="text-4xl tracking-wider font-bold text-center py-4 px-8 space-x-2" >
        <button class="bg-green-300 hover:bg-green-600 py-2 px-4 rounded" onClick={save}>Save</button>
        <button class="bg-blue-300 hover:bg-blue-600 py-2 px-4 rounded" onClick={()=>window.location.reload()}>Clear</button>
        <button class="bg-red-500 hover:bg-red-800 py-2 px-4 rounded" onClick={()=>nav("/")}>Cancel</button>
    </div>
    </div>
    </>
  )
}

export default EditEmployee