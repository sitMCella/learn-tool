import React from 'react'
import { Link } from 'react-router-dom'
import { GOOGLE_AUTH_URL } from '../constants'
import Grid from '@material-ui/core/Grid'
import { GoogleLoginButton } from 'react-social-login-buttons'

function SocialLogin () {
  return (
    <Grid container spacing={1} justify="center" direction="row">
        <Grid item>
            <Link to={GOOGLE_AUTH_URL} style={{ textDecoration: 'none' }}>
                <GoogleLoginButton />
            </Link>
        </Grid>
    </Grid>
  )
}

export default SocialLogin
