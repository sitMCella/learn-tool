import React, { useState } from 'react'
import { ACCESS_TOKEN } from '../constants'
import { executeRequest } from '../restRequests'
import { makeStyles } from '@material-ui/core/styles'
import Grid from '@material-ui/core/Grid'
import Typography from '@material-ui/core/Typography'
import TextField from '@material-ui/core/TextField'
import Button from '@material-ui/core/Button'
import Divider from '@material-ui/core/Divider'
import MuiAlert from '@material-ui/lab/Alert'

function Alert (props) {
  return <MuiAlert elevation={6} variant="filled" {...props} />
}

function LoginForm (props) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loginError, setLoginError] = useState(false)
  const [loginErrorMessage, setLoginErrorMessage] = useState('')

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
        url: '/api/auth/login',
        method: 'POST',
        body: JSON.stringify(loginRequest)
      })
      .then((response) => {
        setLoginError(false)
        localStorage.setItem(ACCESS_TOKEN, response.accessToken)
        console.log('You are successfully logged in.')
        props.history.push({
          pathname: '/',
          key: Math.random()
        })
      }).catch(error => {
        setLoginError(true)
        setLoginErrorMessage(error.message)
        console.log(error.message)
      })
  }

  const useStyles = makeStyles((theme) => ({
    loginFormContent: {
      margin: theme.spacing(1, 0),
      width: '100%'
    },
    input: {
      width: 500,
      '@media only screen and (max-width:768px)': {
        width: 260
      }
    }
  }))
  const classes = useStyles()

  return (
      <form onSubmit={handleSubmit}>
        <Grid container direction="column" justify="center" spacing={2} className={classes.loginFormContent}>
            <Grid item>
                <Divider />
            </Grid>
            <Grid item>
                {loginError && (<Alert severity="error">{loginErrorMessage}</Alert>)}
            </Grid>
            <Grid item>
              <TextField id="email" label="Email" type="email" required variant="outlined" InputProps={{ className: classes.input }} value={email} onChange={emailChangeHandler} />
            </Grid>
            <Grid item>
              <Typography component="h1" variant="h5">
                  <TextField id="password" label="Password" type="password" required variant="outlined" InputProps={{ className: classes.input }} value={password} onChange={passwordChangeHandler} />
              </Typography>
            </Grid>
            <Grid item>
              <Button variant="contained" color="primary" type="submit">Login</Button>
            </Grid>
        </Grid>
      </form>
  )
}

export default LoginForm
