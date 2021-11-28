import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { Button } from '@material-ui/core'
import List from '@material-ui/core/List'
import Card from './Card'
import Toolbar from '@material-ui/core/Toolbar'
import AppBar from '@material-ui/core/AppBar'
import { fade, makeStyles } from '@material-ui/core/styles'
import Drawer from '@material-ui/core/Drawer'
import Divider from '@material-ui/core/Divider'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import DashboardIcon from '@material-ui/icons/Dashboard'
import SaveAltIcon from '@material-ui/icons/SaveAlt'
import SearchIcon from '@material-ui/icons/Search'
import InputBase from '@material-ui/core/InputBase'

const WorkspaceDetails = () => {
  const params = useParams()
  const [list, setList] = useState([])
  const [newCardStatus, setNewCardStatus] = useState(false)
  const [searchParameter, setSearchParameter] = useState([])
  useEffect(() => {
    const getCards = async () => {
      const response = await fetch('/api/workspaces/' + params.name + '/cards', {
        method: 'GET',
        headers: {
          Accepted: 'application/json'
        }
      })
      const responseData = await response.json()
      const loadedCards = []
      for (const key in responseData) {
        loadedCards.push({
          id: responseData[key].id,
          question: responseData[key].question,
          response: responseData[key].response,
          new: false
        })
      }
      setList(loadedCards)
    }
    getCards()
  }, [])
  const searchParameterChangeHandler = (event) => {
    setSearchParameter(event.target.value)
  }
  const keyPressHandler = (event) => {
    if (event.keyCode === 13) {
      const getSearchCards = async () => {
        const content = encodeURIComponent(event.target.value)
        const response = await fetch('/api/workspaces/' + params.name + '/search?content=' + content, {
          method: 'GET',
          headers: {
            Accepted: 'application/json'
          }
        })
        const responseData = await response.json()
        const loadedCards = []
        for (const key in responseData) {
          if (verifyIfCardAlreadyExists(responseData[key].id)) {
            continue
          }
          loadedCards.push({
            id: responseData[key].id,
            question: responseData[key].question,
            response: responseData[key].response,
            new: false
          })
        }
        if (loadedCards.length === 0) {
          return
        }
        const newCards = [...loadedCards, ...list]
        setList(newCards)
      }
      getSearchCards()
    }
  }
  const verifyIfCardAlreadyExists = (cardId) => {
    return list.some(card => card.id === cardId)
  }
  const newCardHandler = () => {
    if (newCardStatus) {
      return
    }
    const newCards = [{ id: null, question: 'Question', response: 'Response', new: true, change: false }, ...list]
    setList(newCards)
    setNewCardStatus(true)
  }
  const createCardHandler = (id, question, response) => {
    const newCards = [{ id: id, question: question, response: response, new: false, change: false }, ...list.slice(1)]
    setList(newCards)
    setNewCardStatus(false)
  }
  const createCardCancelHandler = () => {
    const newCards = list.slice(1)
    setList(newCards)
    setNewCardStatus(false)
  }
  const createCardErrorHandler = () => {
    const newCards = list.slice(1)
    setList(newCards)
    setNewCardStatus(false)
  }
  const updateCardHandler = (cardId) => {
    setNewCardStatus(true)
    const newCards = list.map(card => (card.id === cardId ? { ...card, change: true } : card))
    setList(newCards)
  }
  const updateCardCompleteHandler = (cardId, question, response) => {
    setNewCardStatus(false)
    const newCards = list.map(card => (card.id === cardId ? { ...card, question: question, response: response, change: false } : card))
    setList(newCards)
  }
  const updateCardCancelHandler = (cardId) => {
    setNewCardStatus(false)
    const newCards = list.map(card => (card.id === cardId ? { ...card, change: false } : card))
    setList(newCards)
  }
  const updateCardErrorHandler = (cardId, question, response) => {
    setNewCardStatus(true)
    const newCards = list.map(card => (card.id === cardId ? { ...card, question: question, response: response, change: false } : card))
    setList(newCards)
  }
  const deleteCardCompleteHandler = (cardId) => {
    const index = list.map(card => { return card.id }).indexOf(cardId)
    const newCards = [...list.slice(0, index), ...list.slice(index + 1)]
    setList(newCards)
  }
  const handleExport = () => {
    const exportBackup = async () => {
      const response = await fetch('/api/workspaces/' + params.name + '/export', {
        method: 'GET',
        headers: {
          Accepted: 'application/octet-stream'
        }
      })
      const responseData = await response.blob()
      const url = window.URL.createObjectURL(responseData)
      const a = document.createElement('a')
      a.href = url
      a.download = 'backup.zip'
      a.click()
    }
    exportBackup()
  }
  const useStyles = makeStyles((theme) => ({
    menuButton: {
      marginRight: theme.spacing(2),
      '@media only screen and (max-width:768px)': {
        display: 'none'
      }
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
    search: {
      position: 'relative',
      borderRadius: theme.shape.borderRadius,
      backgroundColor: fade(theme.palette.common.white, 0.15),
      '&:hover': {
        backgroundColor: fade(theme.palette.common.white, 0.25)
      },
      marginRight: theme.spacing(2),
      marginLeft: 0,
      width: '100%',
      [theme.breakpoints.up('sm')]: {
        marginLeft: theme.spacing(3),
        width: 'auto'
      }
    },
    searchIcon: {
      padding: theme.spacing(0, 2),
      height: '100%',
      position: 'absolute',
      pointerEvents: 'none',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center'
    },
    inputRoot: {
      color: 'inherit'
    },
    inputInput: {
      padding: theme.spacing(1, 1, 1, 0),
      // vertical padding + font size from searchIcon
      paddingLeft: `calc(1em + ${theme.spacing(4)}px)`,
      transition: theme.transitions.create('width'),
      width: '100%',
      [theme.breakpoints.up('md')]: {
        width: '20ch'
      }
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
                    <Button color="inherit" onClick={newCardHandler} disabled={newCardStatus}>New Card</Button>
                    <Button color="inherit" component={Link} to={'/workspaces/' + params.name + '/study'}>Study</Button>
                    <div className={classes.search}>
                      <div className={classes.searchIcon}>
                        <SearchIcon />
                      </div>
                      <InputBase
                          placeholder="Search…"
                          classes={{
                            root: classes.inputRoot,
                            input: classes.inputInput
                          }}
                          inputProps={{ 'aria-label': 'search' }}
                          onChange={searchParameterChangeHandler} onKeyDown={keyPressHandler}
                      />
                    </div>
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
                    <ListItem button key="Workspaces" onClick={handleExport}>
                        <ListItemIcon><SaveAltIcon /></ListItemIcon>
                    </ListItem>
                </List>
            </Drawer>
            <div className={classes.content}>
                <List component="nav" aria-label="main mailbox folders">
                    {list.map(card => <Card key={card.id} workspaceName={params.name} id={card.id} question={card.question} response={card.response} selected={false} new={card.new} change={card.change}
    handleCreateCard={createCardHandler} handleCreateCardCancel={createCardCancelHandler} handleUpdateCard={updateCardHandler} handleCraeteCardError={createCardErrorHandler}
    handleUpdateCardComplete={updateCardCompleteHandler} handleUpdateCardCancel={updateCardCancelHandler} handleUpdateCardError={updateCardErrorHandler}
    handleDeleteCardComplete={deleteCardCompleteHandler}/>)}
                </List>
            </div>
        </div>
  )
}

export default WorkspaceDetails
