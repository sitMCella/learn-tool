import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import ProfileMenu from './ProfileMenu'
import AppBar from '@material-ui/core/AppBar'
import Avatar from '@material-ui/core/Avatar'
import Box from '@material-ui/core/Box'
import Divider from '@material-ui/core/Divider'
import Drawer from '@material-ui/core/Drawer'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import Grid from '@material-ui/core/Grid'
import MuiAlert from '@material-ui/lab/Alert'
import Toolbar from '@material-ui/core/Toolbar'
import { makeStyles } from '@material-ui/core/styles'
import DashboardIcon from '@material-ui/icons/Dashboard'
import ListItemIcon from '@material-ui/core/ListItemIcon'

function Alert (props) {
  return <MuiAlert elevation={6} variant="filled" {...props} />
}

function Profile (props) {
  const [profileError, setProfileError] = useState(false)
  const [profileErrorMessage, setProfileErrorMessage] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    if (props.authenticated === false) {
      setProfileError(true)
      setProfileErrorMessage('The user is not authenticated.')
    }
    return () => controller.abort()
  }, [])

  const useStyles = makeStyles((theme) => ({
    appBar: {
      '@media only screen and (max-width:768px)': {
        width: '98%'
      },
      marginBottom: theme.spacing(2),
      marginLeft: 20,
      marginRight: 0
    },
    drawerList: {
      '@media only screen and (max-width:768px)': {
        width: 35,
        paddingLeft: 0
      },
      overflowX: 'hidden',
      width: 60
    },
    drawerListItem: {
      '@media only screen and (max-width:768px)': {
        paddingLeft: 5
      }
    },
    toolbar: {
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'flex-end',
      padding: theme.spacing(0, 1),
      ...theme.mixins.toolbar
    },
    divider: {
      '@media only screen and (max-width:768px)': {
        display: 'none'
      }
    },
    content: {
      '@media only screen and (max-width:14000px)': {
        marginLeft: theme.spacing(5)
      },
      marginRight: 0
    },
    errors: {
      marginBottom: theme.spacing(2)
    },
    profileAvatar: {
      fontSize: 'x-medium',
      textAlign: 'left'
    },
    large: {
      width: theme.spacing(7),
      height: theme.spacing(7)
    },
    textAvatar: {
    },
    profileName: {
    },
    profileEmail: {
    }
  }))
  const classes = useStyles()

  return (
      <Box sx={{ flexGrow: 0 }}>
          <AppBar position="relative" className={classes.appBar}>
              <Toolbar variant="dense">
                  <ProfileMenu {...props} />
              </Toolbar>
          </AppBar>
          <Drawer variant="permanent" anchor="left">
              <div className={classes.toolbar}>
              </div>
              <Divider className={classes.divider} />
              <List className={classes.drawerList}>
                  <ListItem button key="Workspaces" component={Link} to={'/workspaces'} className={classes.drawerListItem}>
                      <ListItemIcon><DashboardIcon /></ListItemIcon>
                  </ListItem>
              </List>
          </Drawer>
          <div className={classes.content}>
              {profileError && (<div className={classes.errors}><Alert severity="error">{profileErrorMessage}</Alert></div>)}
              { !profileError &&
                  (
              <Grid container spacing={1} justify="center" direction="row">
                <Grid item>
                  <Grid container justify="center" direction="column" spacing={0} className={classes.profileAvatar}>
                      <Grid item>
                          {
                              props.currentUser && (
                                props.currentUser.imageUrl
                                  ? (
                                          <img src={props.currentUser.imageUrl} alt={props.currentUser.name}/>
                                    )
                                  : (
                                          <div className={classes.textAvatar}>
                                              <Avatar className={classes.large}>{props.currentUser.name && props.currentUser.name[0]}</Avatar>
                                          </div>
                                    )
                              )
                          }
                      </Grid>
                      {props.currentUser && (
                      <Grid item>
                          <div className={classes.profileName}>
                              <h2>{props.currentUser.name}</h2>
                          </div>
                      </Grid>
                      )}
                      {props.currentUser && (
                      <Grid item>
                          <div className={classes.profileEmail}>
                              <p>{props.currentUser.email}</p>
                          </div>
                      </Grid>
                      )}
                  </Grid>
                </Grid>
              </Grid>
                  )}
          </div>
      </Box>
  )
}

export default Profile
