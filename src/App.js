import Navbar from './components/navbar';
import Employee from './components/employee';
import Add from './components/addEmployee';
import Edit from './components/EditEmployee';
import './App.css';
import { BrowserRouter, Routes , Route } from 'react-router-dom';
import HRContacts from './components/employee';
function App() {
  return (
    <>
    <BrowserRouter>
    <Routes>
    <Route index element ={<HRContacts/>}/>
    <Route path="/addEmployee" element ={<Add/>}/>
    <Route path="/editemployee/:id" element ={<Edit/>}/>
</Routes>    

    </BrowserRouter>
   
    </>
  );
}

export default App;
