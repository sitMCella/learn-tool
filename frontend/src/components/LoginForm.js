import React, { useState } from 'react'
import { ACCESS_TOKEN } from '../constants'
import { executeRequest } from '../restRequests'

function LoginForm (props) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')

  const emailChangeHandler = (event) => {
    setEmail(event.target.value)
  }

  const passwordChangeHandler = (event) => {
    setPassword(event.target.value)
  }

  const handleSubmit = (event) => {
    event.preventDefault()

    const loginRequest = {
      email,
      password
    }

    executeRequest(
      {
        url: '/auth/login',
        method: 'POST',
        body: JSON.stringify(loginRequest)
      })
      .then((response) => {
        localStorage.setItem(ACCESS_TOKEN, response.accessToken)
        console.log('You are successfully logged in.')
        props.history.push('/')
      }).catch(error => {
        console.log(error.message)
      })
  }

  return (
      <form onSubmit={handleSubmit}>
        <div className="form-item">
          <input type="email" name="email"
                 className="form-control" placeholder="Email"
                 value={email} onChange={emailChangeHandler} required/>
        </div>
        <div className="form-item">
          <input type="password" name="password"
                 className="form-control" placeholder="Password"
                 value={password} onChange={passwordChangeHandler} required/>
        </div>
        <div className="form-item">
          <button type="submit" className="btn btn-block btn-primary">Login</button>
        </div>
      </form>
  )
}

export default LoginForm
