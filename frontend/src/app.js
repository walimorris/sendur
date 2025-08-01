import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Home from "../components/Home";
import Configuration from "../components/Configuration";
import Workflows from "../components/Workflows";

const App = () => {

    return (
        <Router>
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/configuration" element={<Configuration/>} />
                <Route path="/workflows" element={<Workflows/>} />
                </Routes>
        </Router>
    );
};

export default App;