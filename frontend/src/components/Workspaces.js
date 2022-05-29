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
  const [list, setList] = useState([])
  const [newWorkspaceStatus, setNewWorkspaceStatus] = useState(false)
  const [workspaceError, setWorkspaceError] = useState(false)
  const [workspaceErrorMessage, setWorkspaceErrorMessage] = useState('')

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
    setList(loadedWorkspaces)
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
    const newWorkspaces = [{ id: -1, name: 'New Workspace', new: true }, ...list]
    setList(newWorkspaces)
    setNewWorkspaceStatus(true)
    setWorkspaceError(false)
  }

  const createWorkspaceHandler = (workspaceId, workspaceName) => {
    const newWorkspaces = [{ id: workspaceId, name: workspaceName, new: false }, ...list.slice(1)]
    setList(newWorkspaces)
    setNewWorkspaceStatus(false)
  }

  const createWorkspaceErrorHandler = (errCode) => {
    const newWorkspaces = list.slice(1)
    setList(newWorkspaces)
    setNewWorkspaceStatus(false)
    setWorkspaceError(true)
    if (errCode === '422') {
      setWorkspaceErrorMessage('Cannot create the Workspace.')
    } else if (errCode === '409') {
      setWorkspaceErrorMessage('Cannot create the Workspace. The Workspace already exists.')
    } else {
      setWorkspaceErrorMessage('Cannot create the Workspace.')
    }
  }

  const createWorkspaceCancelHandler = () => {
    const newWorkspaces = list.slice(1)
    setList(newWorkspaces)
    setNewWorkspaceStatus(false)
  }

  const updateWorkspaceHandler = (workspaceId) => {
    setNewWorkspaceStatus(true)
    const newWorkspaces = list.map(workspace => (workspace.id === workspaceId ? { ...workspace, change: true } : workspace))
    setList(newWorkspaces)
  }

  const updateWorkspaceCompleteHandler = (workspaceId, workspaceName) => {
    setNewWorkspaceStatus(false)
    const newWorkspaces = list.map(workspace => (workspace.id === workspaceId ? { ...workspace, name: workspaceName, change: false } : workspace))
    setList(newWorkspaces)
  }

  const updateWorkspaceCancelHandler = (workspaceId) => {
    setNewWorkspaceStatus(false)
    const newWorkspaces = list.map(workspace => (workspace.id === workspaceId ? { ...workspace, change: false } : workspace))
    setList(newWorkspaces)
  }

  const updateWorkspaceErrorHandler = (workspaceId, workspaceName) => {
    setNewWorkspaceStatus(true)
    const newWorkspaces = list.map(workspace => (workspace.id === workspaceId ? { ...workspace, name: workspaceName, change: false } : workspace))
    setList(newWorkspaces)
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
    importBackup()
      .catch((err) => {
        console.log('Error while importing the backup: ' + err.message)
      })
  }

  const useStyles = makeStyles((theme) => ({
    appBar: {
      '@media only screen and (max-width:768px)': {
        marginLeft: 20
      },
      marginBottom: theme.spacing(2),
      marginLeft: 30,
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
    title: {
      flex: 0,
      display: 'flex',
      position: 'absolute',
      alignItems: 'center',
      fontSize: 'x-large',
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
                    <ListItem button key="Workspaces" className={classes.drawerListItem}>
                        <input style={{ display: 'none' }} id="import" type="file" onChange={handleUploadFileData} />
                        <label htmlFor="import">
                            <ListItemIcon><UploadIcon/></ListItemIcon>
                        </label>
                    </ListItem>
                </List>
            </Drawer>
            <div className={classes.content}>
              {workspaceError && (<div className={classes.errors}><Alert severity="error">{workspaceErrorMessage}</Alert></div>)}
                <div className={classes.title}>Workspaces</div>
                <Box className={classes.events}>
                  <Box className={classes.eventIcon}>
                    <Fab size="small" color="primary" aria-label="add" onClick={newWorkspaceHandler} disabled={newWorkspaceStatus}>
                      <AddIcon />
                    </Fab>
                  </Box>
                </Box>
                <List component="nav" aria-label="main mailbox folders">
                    {list.map(workspace => <Workspace key={workspace.id} id={workspace.id} name={workspace.name} selected={false} new={workspace.new} change={workspace.change}
    handleCreateWorkspace={createWorkspaceHandler} handleCreateWorkspaceError={createWorkspaceErrorHandler} handleCreateWorkspaceCancel={createWorkspaceCancelHandler}
    handleUpdateWorkspace={updateWorkspaceHandler} handleUpdateWorkspaceComplete={updateWorkspaceCompleteHandler} handleUpdateWorkspaceError={updateWorkspaceErrorHandler} handleUpdateWorkspaceCancel={updateWorkspaceCancelHandler}></Workspace>)}
                </List>
            </div>
        </Box>
  )
}

export default Workspaces
