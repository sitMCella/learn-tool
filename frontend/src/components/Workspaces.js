import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { ACCESS_TOKEN, UNAUTHORIZED, UNPROCESSABLE_ENTITY } from '../constants'
import AuthenticationException from '../AuthenticationException'
import Workspace from './Workspace'
import ProfileMenu from './ProfileMenu'
import AppBar from '@material-ui/core/AppBar'
import Box from '@material-ui/core/Box'
import Divider from '@material-ui/core/Divider'
import Drawer from '@material-ui/core/Drawer'
import Fab from '@material-ui/core/Fab'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import Typography from '@material-ui/core/Typography'
import Toolbar from '@material-ui/core/Toolbar'
import MuiAlert from '@material-ui/lab/Alert'
import { makeStyles } from '@material-ui/core/styles'
import AddIcon from '@material-ui/icons/Add'
import DashboardIcon from '@material-ui/icons/Dashboard'
import UploadIcon from '@material-ui/icons/Publish'

function Alert (props) {
  return <MuiAlert elevation={6} variant="filled" {...props} />
}

function Workspaces (props) {
  const [workspaces, setWorkspaces] = useState([])
  const [newWorkspaceStatus, setNewWorkspaceStatus] = useState(false)
  const [workspaceError, setWorkspaceError] = useState(false)
  const [workspaceErrorMessage, setWorkspaceErrorMessage] = useState('')
  const [workspaceImporting, setWorkspaceImporting] = useState(false)

  const getWorkspaces = async (signal) => {
    const headers = {
      Accepted: 'application/json'
    }
    if (localStorage.getItem(ACCESS_TOKEN)) {
      headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
    }
    const response = await fetch('/api/workspaces', {
      method: 'GET',
      headers: headers,
      signal
    })
    if (!response.ok) {
      if (response.status === UNAUTHORIZED || response.status === UNPROCESSABLE_ENTITY) {
        throw new AuthenticationException(JSON.stringify(response))
      }
      throw new Error(JSON.stringify(response))
    }
    const responseData = await response.json()
    const loadedWorkspaces = []
    for (const key in responseData) {
      loadedWorkspaces.push({
        id: responseData[key].id,
        name: responseData[key].name,
        new: false
      })
    }
    setWorkspaces(loadedWorkspaces)
  }

  useEffect(() => {
    const controller = new AbortController()
    const signal = controller.signal
    getWorkspaces(signal)
      .then(() => setWorkspaceError(false))
      .catch((err) => {
        console.log('Error while retrieving the Workspaces: ' + err.message)
        if (err instanceof AuthenticationException) {
          props.history.push({
            pathname: '/login'
          })
        }
        setWorkspaceError(true)
        setWorkspaceErrorMessage('Cannot retrieve the Workspaces, please refresh the page.')
      })
    return () => controller.abort()
  }, [])

  const newWorkspaceHandler = () => {
    if (newWorkspaceStatus) {
      return
    }
    const newWorkspaces = [{ id: -1, name: '', new: true }, ...workspaces]
    setWorkspaces(newWorkspaces)
    setNewWorkspaceStatus(true)
    setWorkspaceError(false)
  }

  const createWorkspaceHandler = (workspaceId, workspaceName) => {
    const newWorkspaces = [{ id: workspaceId, name: workspaceName, new: false }, ...workspaces.slice(1)]
    setWorkspaces(newWorkspaces)
    setNewWorkspaceStatus(false)
  }

  const createWorkspaceErrorHandler = (errCode) => {
    const newWorkspaces = workspaces.slice(1)
    setWorkspaces(newWorkspaces)
    setNewWorkspaceStatus(false)
    setWorkspaceError(true)
    if (errCode === '422') {
      setWorkspaceErrorMessage('Cannot create the Workspace. Please refresh the page.')
    } else if (errCode === '409') {
      setWorkspaceErrorMessage('Cannot create the Workspace. The Workspace already exists.')
    } else {
      setWorkspaceErrorMessage('Cannot create the Workspace.')
    }
  }

  const createWorkspaceCancelHandler = () => {
    const newWorkspaces = workspaces.slice(1)
    setWorkspaces(newWorkspaces)
    setNewWorkspaceStatus(false)
  }

  const updateWorkspaceHandler = (workspaceId) => {
    setNewWorkspaceStatus(true)
    const newWorkspaces = workspaces.map(workspace => (workspace.id === workspaceId ? { ...workspace, change: true } : workspace))
    setWorkspaces(newWorkspaces)
  }

  const updateWorkspaceCompleteHandler = (workspaceId, workspaceName) => {
    setNewWorkspaceStatus(false)
    const newWorkspaces = workspaces.map(workspace => (workspace.id === workspaceId ? { ...workspace, name: workspaceName, change: false } : workspace))
    setWorkspaces(newWorkspaces)
  }

  const updateWorkspaceCancelHandler = (workspaceId) => {
    setNewWorkspaceStatus(false)
    const newWorkspaces = workspaces.map(workspace => (workspace.id === workspaceId ? { ...workspace, change: false } : workspace))
    setWorkspaces(newWorkspaces)
  }

  const updateWorkspaceErrorHandler = (workspaceId, workspaceName) => {
    setNewWorkspaceStatus(true)
    const newWorkspaces = workspaces.map(workspace => (workspace.id === workspaceId ? { ...workspace, name: workspaceName, change: false } : workspace))
    setWorkspaces(newWorkspaces)
  }

  const deleteWorkspaceCompleteHandler = (workspaceId) => {
    const index = workspaces.map(workspace => { return workspace.id }).indexOf(workspaceId)
    const newWorkspaces = [...workspaces.slice(0, index), ...workspaces.slice(index + 1)]
    setWorkspaces(newWorkspaces)
  }

  const deleteWorkspaceErrorHandler = (errCode) => {
    setWorkspaceError(true)
    if (errCode === '422') {
      setWorkspaceErrorMessage('Cannot delete the Workspace. Please refresh the page.')
    } else if (errCode === '404') {
      setWorkspaceErrorMessage('Cannot delete the Workspace. The Workspace does not exist.')
    } else {
      setWorkspaceErrorMessage('Cannot delete the Workspace.')
    }
  }

  const handleUploadFileData = (event) => {
    event.preventDefault()
    const data = new FormData()
    data.append('backup', event.target.files[0])
    const importBackup = async () => {
      const headers = {}
      if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
      }
      const response = await fetch('/api/workspaces/import', {
        method: 'POST',
        headers: headers,
        body: data
      })
      if (!response.ok) {
        console.log('Error while importing the backup: ' + JSON.stringify(response))
        setWorkspaceError(true)
        if (response.status === 409) {
          setWorkspaceErrorMessage('Cannot import the Workspace. The Workspace already exists.')
        } else {
          setWorkspaceErrorMessage('Cannot import the Workspace.')
        }
      } else {
        setWorkspaceError(false)
        getWorkspaces()
          .then(() => setWorkspaceError(false))
          .catch(() => {
            console.log('Error while importing the Workspace.')
            setWorkspaceError(true)
            setWorkspaceErrorMessage('Cannot retrieve the Workspaces, please refresh the page.')
          })
      }
      await response
    }
    setWorkspaceImporting(true)
    importBackup()
      .catch((err) => {
        console.log('Error while importing the backup: ' + err.message)
      })
      .finally(() => {
        setWorkspaceImporting(false)
      })
  }

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
    alerts: {
      marginBottom: theme.spacing(2)
    },
    title: {
      flex: 0,
      display: 'flex',
      position: 'absolute',
      alignItems: 'center',
      padding: theme.spacing(0, 1)
    },
    addIcon: {
      display: 'flex',
      position: 'relative',
      alignItems: 'center',
      marginLeft: 'auto',
      padding: theme.spacing(0, 1)
    },
    events: {
      display: 'flex',
      justifyContent: 'flex-end',
      paddingRight: 5
    },
    eventIcon: {
      paddingRight: 5
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
                <List className={classes.drawerList}>
                    <ListItem button key="Workspaces" className={classes.drawerListItem} disabled={workspaceImporting}>
                        <input style={{ display: 'none' }} id="import" type="file" onChange={handleUploadFileData} />
                        <label htmlFor="import">
                            <ListItemIcon><UploadIcon/></ListItemIcon>
                        </label>
                    </ListItem>
                </List>
            </Drawer>
            <div className={classes.content}>
              {workspaceError && (<div className={classes.alerts}><Alert severity="error">{workspaceErrorMessage}</Alert></div>)}
              {workspaceImporting && (<div className={classes.alerts}><Alert severity="info">Importing Workspace</Alert></div>)}
                <div className={classes.title}>
                  <Typography variant="h5" color="textSecondary" component="p" gutterBottom>
                    Workspaces
                  </Typography>
                </div>
                <Box className={classes.events}>
                  <Box className={classes.eventIcon}>
                    <Fab size="small" color="primary" aria-label="add" onClick={newWorkspaceHandler} disabled={newWorkspaceStatus}>
                      <AddIcon />
                    </Fab>
                  </Box>
                </Box>
                <List component="nav" aria-label="main mailbox folders">
                    {workspaces.map(workspace => <Workspace key={workspace.id} id={workspace.id} name={workspace.name} selected={false} new={workspace.new} change={workspace.change}
    handleCreateWorkspace={createWorkspaceHandler} handleCreateWorkspaceError={createWorkspaceErrorHandler} handleCreateWorkspaceCancel={createWorkspaceCancelHandler}
    handleUpdateWorkspace={updateWorkspaceHandler} handleUpdateWorkspaceComplete={updateWorkspaceCompleteHandler} handleUpdateWorkspaceError={updateWorkspaceErrorHandler} handleUpdateWorkspaceCancel={updateWorkspaceCancelHandler}
    handleDeleteWorkspaceComplete={deleteWorkspaceCompleteHandler} handleDeleteWorkspaceError={deleteWorkspaceErrorHandler}/>)}
                </List>
            </div>
        </Box>
  )
}

export default Workspaces
