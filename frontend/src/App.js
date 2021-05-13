import React, { Component } from 'react';
import { BrowserRouter, Route, Switch, Redirect } from 'react-router-dom';
import './App.css';
import Workspaces from "./components/Workspaces";
import WorkspaceDetails from "./components/WorkspaceDetails";
import Container from '@material-ui/core/Container';

class App extends Component {
  render() {
    return (
      <div className="App">
          <Container maxWidth="lg">
              <BrowserRouter>
                  <Switch>
                      <Redirect exact from="/" to="/workspaces" />
                      <Route exact path="/workspaces" component={Workspaces} />
                      <Redirect exact from="/workspaces/:name" to="/workspaces/:name/cards" />
                      <Route path="/workspaces/:name/cards" children={<WorkspaceDetails />} />
                  </Switch>
              </BrowserRouter>
          </Container>
      </div>
    );
  }
}

export default App;
