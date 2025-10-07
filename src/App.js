import Navbar from './components/navbar';
import Employee from './components/employee';
import Add from './components/addEmployee';
import Edit from './components/EditEmployee';
import './App.css';
import { BrowserRouter, Routes , Route } from 'react-router-dom';
function App() {
  return (
    <>
    <BrowserRouter>
    <Navbar/>
    <Routes>
    <Route index element ={<Employee/>}/>
    <Route path="/addEmployee" element ={<Add/>}/>
    <Route path="/EditEmployee" element ={<Edit/>}/>
</Routes>    

    </BrowserRouter>
   
    </>
  );
}

export default App;
