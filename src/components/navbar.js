import React from 'react'

const navbar = () => {
  return (
    <div className="bg-blue-200 h-16 items-center flex">
     <h1 className="text-3xl font-bold ml-4">🧑🏻‍💻 EMPLOYEE SERVICES </h1>
     <div className="space-x-4 ml-auto pr-5">
     <a class="hover:text-blue-800" href="\">Home</a>
     <a class="hover:text-blue-800" href ="\">Profile</a>
     <a class="hover:text-blue-800" href="\">Logout</a>
     </div> 
    </div>
  )
}
export default navbar