import React, { useState } from 'react'
import { executeRequest } from '../restRequests'
import Grid from '@material-ui/core/Grid'
import TextField from '@material-ui/core/TextField'
import Typography from '@material-ui/core/Typography'
import Button from '@material-ui/core/Button'
import MuiAlert from '@material-ui/lab/Alert'
import { makeStyles } from '@material-ui/core/styles'

function Alert (props) {
  return <MuiAlert elevation={6} variant="filled" {...props} />
}

function Signup (props) {
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [signUpError, setSignUpError] = useState(false)
  const [signUpErrorMessage, setSignUpErrorMessage] = useState('')

  const nameChangeHandler = (event) => {
    setName(event.target.value)
  }

  const emailChangeHandler = (event) => {
    setEmail(event.target.value)
  }

  const passwordChangeHandler = (event) => {
    setPassword(event.target.value)
  }

  const handleSubmit = (event) => {
    event.preventDefault()
    const signUpRequest = {
      name,
      email,
      password
    }

    executeRequest(
      {
        url: '/api/auth/signup',
        method: 'POST',
        body: JSON.stringify(signUpRequest)
      })
      .then(() => {
        setSignUpError(false)
        console.log('You are successfully registered. Please login to continue.')
        props.history.push({
          pathname: '/login'
        })
      }).catch(error => {
        setSignUpError(true)
        setSignUpErrorMessage(error.message)
        console.log(error.message)
      })
  }

  const useStyles = makeStyles((theme) => ({
    formContent: {
      fontSize: 'x-medium'
    },
    loginFormContent: {
      width: 520,
      margin: theme.spacing(1, 0),
      '@media only screen and (max-width:768px)': {
        width: 310
      }
    },
    title: {
      margin: theme.spacing(2, 0)
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
    <Grid container spacing={0} justify="center" direction="row" className={classes.formContent}>
        <Grid item>
            <form onSubmit={handleSubmit}>
                <Grid container direction="column" justify="center" spacing={2} className={classes.loginFormContent}>
                    <Grid item>
                        <Typography component="div" variant="h4">
                            <div className={classes.title}>Sign up to Learn Tool</div>
                        </Typography>
                    </Grid>
                    <Grid item>
                        {signUpError && (<Alert severity="error">{signUpErrorMessage}</Alert>)}
                    </Grid>
                    <Grid item>
                        <TextField id="name" label="Name" required variant="outlined" InputProps={{ className: classes.input }} value={name} onChange={nameChangeHandler} />
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
                        <Button variant="contained" color="primary" type="submit">Sign Up</Button>
                    </Grid>
                </Grid>
            </form>
        </Grid>
    </Grid>
  )
}

export default Signup
