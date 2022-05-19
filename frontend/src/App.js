import React, { Component } from 'react'
import { BrowserRouter, Route, Switch, Redirect } from 'react-router-dom'
import './App.css'
import { ACCESS_TOKEN } from './constants'
import { executeRequest } from './restRequests'
import Login from './components/Login'
import NotFound from './components/NotFound'
import OAuth2RedirectHandler from './components/OAuth2RedirectHandler'
import Workspaces from './components/Workspaces'
import WorkspaceDetails from './components/WorkspaceDetails'
import Profile from './components/Profile'
import Signup from './components/Signup'
import Study from './components/Study'
import Container from '@material-ui/core/Container'

class App extends Component {
  constructor (props) {
    super(props)
    this.state = {
      authenticated: false,
      currentUser: null,
      loading: true
    }

    this.loadCurrentlyLoggedInUser = this.loadCurrentlyLoggedInUser.bind(this)
    this.handleLogout = this.handleLogout.bind(this)
  }

  getCurrentUser () {
    if (!localStorage.getItem(ACCESS_TOKEN)) {
      // eslint-disable-next-line prefer-promise-reject-errors
      return Promise.reject('No access token set.')
    }
    return executeRequest({
      url: '/api/user/me',
      method: 'GET'
    })
  }

  loadCurrentlyLoggedInUser () {
    this.getCurrentUser()
      .then(response => {
        this.setState({
          currentUser: response,
          authenticated: true,
          loading: false
        })
      }).catch((err) => {
        console.log(err)
        this.setState({
          currentUser: null,
          authenticated: false,
          loading: false
        })
      })
  }

  handleLogout () {
    localStorage.removeItem(ACCESS_TOKEN)
    this.setState({
      authenticated: false,
      currentUser: null
    })
    console.log('You are safely logged out.')
  }

  componentDidMount () {
    this.loadCurrentlyLoggedInUser()
  }

  render () {
    if (this.state.loading) {
      return (
        <div></div>
      )
    }

    return (
      <div className="App">
          <Container>
              <BrowserRouter>
                  <Switch>
                      <Redirect exact from="/" to="/workspaces" />
                      <Route path="/login" render={(props) => <Login authenticated={this.state.authenticated} loadCurrentLoggedInUser={this.loadCurrentlyLoggedInUser} {...props} />} />
                      <Route path="/oauth2/redirect" render={(props) => <OAuth2RedirectHandler {...props} />} />
                      <Route path="/signup" render={(props) => <Signup authenticated={this.state.authenticated} {...props} />}/>
                      <Route path="/profile" render={(props) => <Profile authenticated={this.state.authenticated} currentUser={this.state.currentUser} onLogout={this.handleLogout} {...props} />} />
                      <Route exact path="/workspaces" render={(props) => <Workspaces key={Math.random()} onLogout={this.handleLogout} {...props} />} />
                      <Route path="/workspaces/:name/cards" render={(props) => <WorkspaceDetails onLogout={this.handleLogout} {...props} />}/>
                      <Route path="/workspaces/:name/study" render={(props) => <Study onLogout={this.handleLogout} {...props} />}/>
                      <Route component={NotFound}/>
                  </Switch>
              </BrowserRouter>
          </Container>
      </div>
    )
  }
}

export default App
