import React, { Component } from 'react';
import './App.css';
import Workspaces from "./components/Workspaces";

class App extends Component {
  render() {
    return (
      <div className="App">
          <Workspaces></Workspaces>
      </div>
    );
  }
}

export default App;
