import React, { useEffect } from 'react'
import { Link, Redirect } from 'react-router-dom'
import LoginForm from './LoginForm'
import SocialLogin from './SocialLogin'
import Grid from '@material-ui/core/Grid'
import Paper from '@material-ui/core/Paper'
import Typography from '@material-ui/core/Typography'
import { makeStyles } from '@material-ui/core/styles'

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

  const useStyles = makeStyles((theme) => ({
    formContent: {
      width: 1200,
      fontSize: 'x-medium'
    },
    loginFormContent: {
      width: 520,
      margin: theme.spacing(1, 0),
      '@media only screen and (max-width:768px)': {
        width: 270
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
        <Grid container spacing={0} justify="center" direction="row" className={classes.formContent}>
            <Grid item>
                <Grid container direction="column" justify="center" spacing={2} className={classes.loginFormContent}>
                    <Paper variant="elevation" elevation={2}>
                        <Grid item>
                            <Typography component="div" variant="h4">
                                <div className={classes.title}>Login to Learn Tool</div>
                            </Typography>
                        </Grid>
                        <Grid item>
                            <SocialLogin />
                        </Grid>
                        <Grid item>
                            <LoginForm {...props} />
                        </Grid>
                        <Grid item>
                            <Typography component="div" variant="subtitle1" gutterBottom>
                                <div>New user? <Link to="/signup">Sign up!</Link></div>
                            </Typography>
                        </Grid>
                    </Paper>
                </Grid>
            </Grid>
        </Grid>
    )
  }
}

export default Login
