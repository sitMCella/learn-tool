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
      loading: true,
      settings: {
        cardQuestionTextColor: '#0000008A',
        cardResponseTextColor: '#0000008A',
        studyCardQuestionBackgroundColor: '#EFD469',
        studyCardQuestionTextColor: '#000000A6',
        studyCardResponseBackgroundColor: '#FFFFFFFF',
        studyCardResponseTextColor: '#000000A6'
      }
    }
    this.loadCurrentlyLoggedInUser = this.loadCurrentlyLoggedInUser.bind(this)
    this.handleLogout = this.handleLogout.bind(this)
    this.handleSettingsUpdate = this.handleSettingsUpdate.bind(this)
    this.assignCssVariables = this.assignCssVariables.bind(this)
    this.assignCssVariables(this.state.settings)
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
          ...this.state,
          currentUser: response,
          authenticated: true,
          loading: false
        })
      }).catch((err) => {
        console.log(err)
        this.setState({
          ...this.state,
          currentUser: null,
          authenticated: false,
          loading: false
        })
      })
  }

  handleLogout () {
    localStorage.removeItem(ACCESS_TOKEN)
    this.setState({
      ...this.state,
      authenticated: false,
      currentUser: null
    })
    console.log('You are safely logged out.')
  }

  assignCssVariables (settings) {
    document.documentElement.style.setProperty('--card-question-text-color', settings.cardQuestionTextColor)
    document.documentElement.style.setProperty('--card-response-text-color', settings.cardResponseTextColor)
    document.documentElement.style.setProperty('--study-card-question-background-color', settings.studyCardQuestionBackgroundColor)
    document.documentElement.style.setProperty('--study-card-question-text-color', settings.studyCardQuestionTextColor)
    document.documentElement.style.setProperty('--study-card-response-background-color', settings.studyCardResponseBackgroundColor)
    document.documentElement.style.setProperty('--study-card-response-text-color', settings.studyCardResponseTextColor)
  }

  handleSettingsUpdate (settings) {
    this.setState({
      ...this.state,
      settings: settings
    })
    this.assignCssVariables(settings)
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
                      <Redirect exact from="/" to="/login" />
                      <Route path="/login" render={(props) => <Login authenticated={this.state.authenticated} loadCurrentLoggedInUser={this.loadCurrentlyLoggedInUser} {...props} />} />
                      <Route path="/oauth2/redirect" render={(props) => <OAuth2RedirectHandler {...props} />} />
                      <Route path="/signup" render={(props) => <Signup authenticated={this.state.authenticated} {...props} />}/>
                      <Route path="/profile" render={(props) => <Profile authenticated={this.state.authenticated} currentUser={this.state.currentUser} onLogout={this.handleLogout} {...props} />} />
                      <Route exact path="/workspaces" render={(props) => <Workspaces key={Math.random()} onLogout={this.handleLogout} {...props} />} />
                      <Route path="/workspaces/:id/cards" render={(props) => <WorkspaceDetails onLogout={this.handleLogout} onSettingsUpdate={this.handleSettingsUpdate} settings={this.state.settings} {...props} />}/>
                      <Route path="/workspaces/:id/study" render={(props) => <Study onLogout={this.handleLogout} onSettingsUpdate={this.handleSettingsUpdate} settings={this.state.settings} {...props} />}/>
                      <Route component={NotFound}/>
                  </Switch>
              </BrowserRouter>
          </Container>
      </div>
    )
  }
}

export default App
