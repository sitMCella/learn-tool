import React from 'react'
import { GOOGLE_AUTH_URL } from '../constants'
import Grid from '@material-ui/core/Grid'
import { GoogleLoginButton } from 'react-social-login-buttons'

function SocialLogin () {
  return (
    <Grid container spacing={1} justify="center" direction="row">
        <Grid item>
            <a href={GOOGLE_AUTH_URL}>
                <GoogleLoginButton />
            </a>
        </Grid>
    </Grid>
  )
}

export default SocialLogin
