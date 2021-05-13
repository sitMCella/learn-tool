import React, { Component } from 'react';
import { BrowserRouter, Route, Switch, Redirect } from 'react-router-dom';
import './App.css';
import Workspaces from "./components/Workspaces";

class App extends Component {
  render() {
    return (
      <div className="App">
        <BrowserRouter>
          <Switch>
            <Route exact path="/"
                render={() => {
                  return (<Redirect to="/workspaces" />)
                }}
            />
            <Route path="/workspaces" component={Workspaces} />
          </Switch>
        </BrowserRouter>
      </div>
    );
  }
}

export default App;
