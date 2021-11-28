import React, { useState, useEffect } from 'react'
import Workspace from './Workspace'
import { makeStyles } from '@material-ui/core/styles'
import List from '@material-ui/core/List'
import { Button } from '@material-ui/core'
import MuiAlert from '@material-ui/lab/Alert'
import AppBar from '@material-ui/core/AppBar'
import Toolbar from '@material-ui/core/Toolbar'
import Divider from '@material-ui/core/Divider'
import ListItem from '@material-ui/core/ListItem'
import { Link } from 'react-router-dom'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import DashboardIcon from '@material-ui/icons/Dashboard'
import Drawer from '@material-ui/core/Drawer'
import UploadIcon from '@material-ui/icons/Publish'

function Alert (props) {
  return <MuiAlert elevation={6} variant="filled" {...props} />
}

function Workspaces () {
  const [list, setList] = useState([])
  const [newWorkspaceStatus, setNewWorkspaceStatus] = useState(false)
  const [workspaceError, setWorkspaceError] = useState(false)
  const [workspaceErrorMessage, setWorkspaceErrorMessage] = useState('')
  const getWorkspaces = async () => {
    const response = await fetch('/api/workspaces', {
      method: 'GET',
      headers: {
        Accepted: 'application/json'
      }
    })
    const responseData = await response.json()
    const loadedWorkspaces = []
    for (const key in responseData) {
      loadedWorkspaces.push({
        name: responseData[key].name,
        new: false
      })
    }
    setList(loadedWorkspaces)
  }
  useEffect(() => {
    getWorkspaces()
      .then(() => setWorkspaceError(false))
      .catch(() => {
        setWorkspaceError(true)
        setWorkspaceErrorMessage('Cannot retrieve the Workspaces, please refresh the page.')
      })
  }, [])
  const newWorkspaceHandler = () => {
    if (newWorkspaceStatus) {
      return
    }
    const newWorkspaces = [{ name: 'New Workspace Name', new: true }, ...list]
    setList(newWorkspaces)
    setNewWorkspaceStatus(true)
    setWorkspaceError(false)
  }
  const submitHandler = (workspaceName) => {
    const newWorkspaces = [{ name: workspaceName, new: false }, ...list.slice(1)]
    setList(newWorkspaces)
    setNewWorkspaceStatus(false)
  }
  const createErrorHandler = (errCode) => {
    const newWorkspaces = list.slice(1)
    setList(newWorkspaces)
    setNewWorkspaceStatus(false)
    setWorkspaceError(true)
    if (errCode === '422') {
      setWorkspaceErrorMessage('Cannot create the Workspace. The Workspace name should not contain spaces.')
    } else if (errCode === '409') {
      setWorkspaceErrorMessage('Cannot create the Workspace. The Workspace already exists.')
    } else {
      setWorkspaceErrorMessage('Cannot create the Workspace.')
    }
  }
  const cancelButtonClickHandler = () => {
    const newWorkspaces = list.slice(1)
    setList(newWorkspaces)
    setNewWorkspaceStatus(false)
  }
  const handleUploadFileData = (event) => {
    event.preventDefault()
    const data = new FormData()
    data.append('backup', event.target.files[0])
    const importBackup = async () => {
      const response = await fetch('/api/workspaces/import', {
        method: 'POST',
        body: data
      })
      if (!response.ok) {
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
            setWorkspaceError(true)
            setWorkspaceErrorMessage('Cannot retrieve the Workspaces, please refresh the page.')
          })
          .catch(() => { console.log('Error while importing the Workspace.') })
      }
      await response
    }
    importBackup()
  }
  const useStyles = makeStyles((theme) => ({
    menuButton: {
      marginRight: theme.spacing(2),
      '@media only screen and (max-width:768px)': {
        display: 'none'
      }
    },
    title: {
      marginRight: theme.spacing(10)
    },
    appBar: {
      '@media only screen and (max-width:14000px)': {
        marginLeft: theme.spacing(5)
      },
      marginBottom: theme.spacing(2),
      zIndex: theme.zIndex.drawer + 1,
      transition: theme.transitions.create(['width', 'margin'], {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen
      })
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
      }
    }
  }))
  const classes = useStyles()
  return (
        <div>
            <AppBar position="static" className={classes.appBar}>
                <Toolbar>
                    <Button color="inherit" onClick={newWorkspaceHandler} disabled={newWorkspaceStatus}>New Workspace</Button>
                </Toolbar>
            </AppBar>
            <Drawer variant="permanent" anchor="left">
                <div className={classes.toolbar}>
                </div>
                <Divider className={classes.divider} />
                <List>
                    <ListItem button key="Workspaces" component={Link} to={'/workspaces'}>
                        <ListItemIcon><DashboardIcon /></ListItemIcon>
                    </ListItem>
                </List>
                <List>
                    <ListItem button key="Workspaces">
                        <input style={{ display: 'none' }} id="import" type="file" onChange={handleUploadFileData} />
                        <label htmlFor="import">
                            <ListItemIcon><UploadIcon/></ListItemIcon>
                        </label>
                    </ListItem>
                </List>
            </Drawer>
            <div className={classes.content}>
                {workspaceError && (<Alert severity="error">{workspaceErrorMessage}</Alert>)}
                <List component="nav" aria-label="main mailbox folders">
                    {list.map(workspace => <Workspace key={workspace.name} name={workspace.name} selected={false} new={workspace.new} handleSubmit={submitHandler} handleError={createErrorHandler} handleCancel={cancelButtonClickHandler}></Workspace>)}
                </List>
            </div>
        </div>
  )
}

export default Workspaces
