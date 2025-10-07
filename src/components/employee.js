import React, { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import EmployeeService from "../Service/EmployeeService";

const Employee = () => {
  const [employees, setEmployees] = useState(null);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState(""); // new state for search
  const nav = useNavigate();

  // Fetch employees whenever search changes
  useEffect(() => {
    const fetch = async () => {
      setLoading(true);
      try {
        let response;
        if (search === "") {
          response = await EmployeeService.getEmployee(); // get all employees
        } else {
          response = await EmployeeService.searchEmployee(search); // call search API
        }
        setEmployees(response.data);
        setLoading(false);
      } catch (e) {
        console.log(e);
      }
    };
    fetch();
  }, [search]);

  const del = (e, id) => {
    e.preventDefault();
    EmployeeService.delete(id).then(() => {
      if (employees) {
        setEmployees((prevElement) =>
          prevElement.filter((employee) => employee.id !== id)
        );
      }
    });
  };

  return (
    <div className="container mx-auto my-8">
      <div>
        <button
          onClick={() => nav("/addEmployee")}
          className="bg-slate-300 hover:bg-blue-600 h-10 px-20 py-2 items-center my-10 font-semibold rounded"
        >
          Add Employee 🧑‍💼
        </button>
      </div>
    
      {/* Search Input */}
      <div className="mb-4">
        <input
          type="text"
          placeholder="Search by name or email"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="border border-gray-400 p-2 rounded w-1/2"
        />
      </div>

      <div>
        
        <table className="shadow">
         

          <thead className="bg-slate-300 p-20">
            <tr>
              <th className="px-6 py-3 uppercase tracking-wide">Id</th>
              <th className="px-6 py-3 uppercase tracking-wide">Name</th>
              <th className="px-6 py-3 uppercase tracking-wide">Email</th>
              <th className="px-6 py-3 uppercase tracking-wide">Actions</th>
              <th className="px-6 py-3">
        <button
          onClick={() => window.location.href = "http://localhost:9000/users/export"}
          className="bg-green-500 text-white px-4 py-1 rounded"
        >
          Export to CSV 📄
        </button>
      </th>
            </tr>
          </thead>
          {!loading && (
            <tbody>
              {employees.map((employee) => (
                <tr className="hover:bg-red-300" key={employee.id}>
                  <td className="text-left px-6 py-3 whitespace-nowrap">
                    {employee.id}
                  </td>
                  <td className="text-left px-6 py-3 whitespace-nowrap">
                    {employee.name}
                  </td>
                  <td className="text-left px-6 py-3 whitespace-nowrap">
                    {employee.email}
                  </td>
                  <td className="text-left px-6 py-3 ">
                    <Link to="/EditEmployee" className="hover:text-blue-800 pr-5">
                      Edit ✏️
                    </Link>
                    <a
                      className="hover:text-blue-800"
                      href="/"
                      onClick={(e) => del(e, employee.id)}
                    >
                      Delete 🗑️
                    </a>
                  </td>
                </tr>
              ))}
            </tbody>
          )}
        </table>
      </div>
    </div>
  );
};

export default Employee;
