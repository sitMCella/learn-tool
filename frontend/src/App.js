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
import PrivateRedirect from './components/PrivateRedirect'
import PrivateRoute from './components/PrivateRoute'
import Profile from './components/Profile'
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
        <div className="loading-indicator" style={{ display: 'block', textAlign: 'center', marginTop: '30px' }}>
          Loading ...
        </div>
      )
    }

    return (
      <div className="App">
          <Container >
              <BrowserRouter>
                  <Switch>
                      <Redirect exact from="/" to="/workspaces" />
                      <Route path="/login" render={(props) => <Login authenticated={this.state.authenticated} {...props} />} />
                      <Route path="/oauth2/redirect" component={OAuth2RedirectHandler} />
                      <PrivateRoute path="/profile" authenticated={this.state.authenticated} currentUser={this.state.currentUser} component={Profile}/>
                      <Route exact path="/workspaces" component={Workspaces} />
                      <Route path="/workspaces/:name/cards">
                          <WorkspaceDetails />
                      </Route>
                      <Route path="/workspaces/:name/study">
                          <Study />
                      </Route>
                      <Route component={NotFound}/>
                  </Switch>
              </BrowserRouter>
          </Container>
      </div>
    )
  }
}

export default App
