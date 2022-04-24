import React, { useEffect } from 'react'
import { Link, Redirect } from 'react-router-dom'
import LoginForm from './LoginForm'
import SocialLogin from './SocialLogin'

function Login (props) {
  useEffect(() => {
    if (props.location.state && props.location.state.error) {
      setTimeout(() => {
        console.log(props.location.state.error)
        props.history.replace({
          pathname: props.location.pathname,
          state: {}
        })
      }, 100)
    }
  }, [])

  if (props.authenticated) {
    return (
            <Redirect
            to={{
              pathname: '/',
              state: { from: props.location }
            }}/>
    )
  } else {
    return (
          <div className="login-container">
              <div className="login-content">
                  <h1 className="login-title">Login to LearnTool</h1>
                  <SocialLogin />
                  <div className="or-separator">
                      <span className="or-text">OR</span>
                  </div>
                  <LoginForm {...props} />
                  <span className="signup-link">New user? <Link to="/signup">Sign up!</Link></span>
              </div>
          </div>
    )
  }
}

export default Login
