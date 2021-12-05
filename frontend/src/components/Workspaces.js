import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import Workspace from './Workspace'
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

function Workspaces () {
  const [list, setList] = useState([])
  const [newWorkspaceStatus, setNewWorkspaceStatus] = useState(false)
  const [workspaceError, setWorkspaceError] = useState(false)
  const [workspaceErrorMessage, setWorkspaceErrorMessage] = useState('')

  const getWorkspaces = async (signal) => {
    const response = await fetch('/api/workspaces', {
      method: 'GET',
      headers: {
        Accepted: 'application/json'
      },
      signal
    })
    if (!response.ok) {
      throw new Error(JSON.stringify(response))
    }
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
    const controller = new AbortController()
    const signal = controller.signal
    getWorkspaces(signal)
      .then(() => setWorkspaceError(false))
      .catch((err) => {
        console.log('Error while retrieving the Workspaces: ' + err.message)
        setWorkspaceError(true)
        setWorkspaceErrorMessage('Cannot retrieve the Workspaces, please refresh the page.')
      })
    return () => controller.abort()
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
            setWorkspaceError(true)
            setWorkspaceErrorMessage('Cannot retrieve the Workspaces, please refresh the page.')
          })
          .catch(() => { console.log('Error while importing the Workspace.') })
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
      marginBottom: theme.spacing(2)
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
      }
    },
    title: {
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
    }
  }))
  const classes = useStyles()

  return (
        <Box sx={{ flexGrow: 1 }}>
            <AppBar position="static" className={classes.appBar}>
                <Toolbar variant="dense">
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
                {workspaceError && (<Alert severity="error">{workspaceErrorMessage}</Alert>)}
                <div className={classes.title}>Workspaces</div>
                <Fab size="small" color="primary" className={classes.addIcon} aria-label="add" onClick={newWorkspaceHandler} disabled={newWorkspaceStatus}>
                    <AddIcon />
                </Fab>
                <List component="nav" aria-label="main mailbox folders">
                    {list.map(workspace => <Workspace key={workspace.name} name={workspace.name} selected={false} new={workspace.new} handleSubmit={submitHandler} handleError={createErrorHandler} handleCancel={cancelButtonClickHandler}></Workspace>)}
                </List>
            </div>
        </Box>
  )
}

export default Workspaces
