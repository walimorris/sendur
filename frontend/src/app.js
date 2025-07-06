import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Home from "../components/Home";
import Configuration from "../components/Configuration";

const App = () => {

    return (
        <Router>
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/configuration" element={<Configuration/>} />
                </Routes>
        </Router>
    );
};

export default App;