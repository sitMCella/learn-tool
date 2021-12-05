import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import Card from './Card'
import AppBar from '@material-ui/core/AppBar'
import Button from '@material-ui/core/Button'
import Drawer from '@material-ui/core/Drawer'
import Divider from '@material-ui/core/Divider'
import InputBase from '@material-ui/core/InputBase'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import Toolbar from '@material-ui/core/Toolbar'
import { fade, makeStyles } from '@material-ui/core/styles'
import DashboardIcon from '@material-ui/icons/Dashboard'
import CloseIcon from '@material-ui/icons/Close'
import SaveAltIcon from '@material-ui/icons/SaveAlt'
import SearchIcon from '@material-ui/icons/Search'

const WorkspaceDetails = () => {
  const params = useParams()
  const [cards, setCards] = useState([])
  const [newCardStatus, setNewCardStatus] = useState(false)
  const [backupCards, setBackupCards] = useState([])
  const [searchPattern, setSearchPattern] = useState('')
  const [typingTimeout, setTypingTimeout] = useState()

  const getCards = async (signal) => {
    const response = await fetch('/api/workspaces/' + params.name + '/cards', {
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
    const loadedCards = []
    for (const key in responseData) {
      loadedCards.push({
        id: responseData[key].id,
        question: responseData[key].question,
        response: responseData[key].response,
        new: false
      })
    }
    setCards(loadedCards)
    setBackupCards(loadedCards)
  }

  useEffect(() => {
    const controller = new AbortController()
    const signal = controller.signal
    getCards(signal)
      .catch((err) => {
        console.log('Error while retrieving the cards from the Workspace ' + params.name + ': ' + err.message)
      })
    return () => controller.abort()
  }, [])

  const resetSearchHandler = () => {
    setSearchPattern('')
    setCards(backupCards)
  }

  const searchOnChangeHandler = (event) => {
    const getSearchCards = async () => {
      const content = encodeURIComponent(event.target.value)
      const response = await fetch('/api/workspaces/' + params.name + '/search?content=' + content, {
        method: 'GET',
        headers: {
          Accepted: 'application/json'
        }
      })
      if (!response.ok) {
        throw new Error(JSON.stringify(response))
      }
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
      setCards(loadedCards)
    }
    setSearchPattern(event.target.value)
    if (event.target.value === '') {
      setCards(backupCards)
      return
    }
    if (typingTimeout) clearTimeout(typingTimeout)
    setTypingTimeout(setTimeout(() => {
      if (event.target.value === '') {
        setCards(backupCards)
        return
      }
      getSearchCards()
        .catch((err) => {
          console.log('Error while searching the Cards: ' + err.message)
        })
    }, 500))
  }

  const newCardHandler = () => {
    if (newCardStatus) {
      return
    }
    const newCards = [{ id: null, question: 'Question', response: 'Response', new: true, change: false }, ...cards]
    setCards(newCards)
    setNewCardStatus(true)
  }

  const createCardHandler = (id, question, response) => {
    const newCards = [{ id: id, question: question, response: response, new: false, change: false }, ...cards.slice(1)]
    setCards(newCards)
    setBackupCards(newCards)
    setNewCardStatus(false)
  }
  const createCardCancelHandler = () => {
    const newCards = cards.slice(1)
    setCards(newCards)
    setNewCardStatus(false)
  }
  const createCardErrorHandler = () => {
    const newCards = cards.slice(1)
    setCards(newCards)
    setNewCardStatus(false)
  }
  const updateCardHandler = (cardId) => {
    setNewCardStatus(true)
    const newCards = cards.map(card => (card.id === cardId ? { ...card, change: true } : card))
    setCards(newCards)
  }
  const updateCardCompleteHandler = (cardId, question, response) => {
    setNewCardStatus(false)
    const newCards = cards.map(card => (card.id === cardId ? { ...card, question: question, response: response, change: false } : card))
    setCards(newCards)
  }
  const updateCardCancelHandler = (cardId) => {
    setNewCardStatus(false)
    const newCards = cards.map(card => (card.id === cardId ? { ...card, change: false } : card))
    setCards(newCards)
  }
  const updateCardErrorHandler = (cardId, question, response) => {
    setNewCardStatus(true)
    const newCards = cards.map(card => (card.id === cardId ? { ...card, question: question, response: response, change: false } : card))
    setCards(newCards)
  }
  const deleteCardCompleteHandler = (cardId) => {
    const index = cards.map(card => { return card.id }).indexOf(cardId)
    const newCards = [...cards.slice(0, index), ...cards.slice(index + 1)]
    setCards(newCards)
  }

  const handleExport = () => {
    const exportBackup = async () => {
      const response = await fetch('/api/workspaces/' + params.name + '/export', {
        method: 'GET',
        headers: {
          Accepted: 'application/octet-stream'
        }
      })
      if (!response.ok) {
        throw new Error(JSON.stringify(response))
      }
      const responseData = await response.blob()
      const url = window.URL.createObjectURL(responseData)
      const a = document.createElement('a')
      a.href = url
      a.download = 'backup.zip'
      a.click()
    }
    exportBackup()
      .catch((err) => {
        console.log('Error while exporting the backup: ' + err.message)
      })
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
    closeIcon: {
      height: '100%',
      pointerEvents: 'all',
      alignItems: 'center',
      display: 'flex',
      justifyContent: 'center',
      cursor: 'pointer'
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
                          value={searchPattern}
                          onChange={searchOnChangeHandler}
                          endAdornment={<div className={classes.closeIcon}><CloseIcon onClick={resetSearchHandler} /></div>}
                      />
                    </div>
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
                    <ListItem button key="Workspaces" onClick={handleExport} className={classes.drawerListItem}>
                        <ListItemIcon><SaveAltIcon /></ListItemIcon>
                    </ListItem>
                </List>
            </Drawer>
            <div className={classes.content}>
                <List component="nav" aria-label="main mailbox folders">
                    {cards.map(card => <Card key={card.id} workspaceName={params.name} id={card.id} question={card.question} response={card.response} selected={false} new={card.new} change={card.change}
    handleCreateCard={createCardHandler} handleCreateCardCancel={createCardCancelHandler} handleUpdateCard={updateCardHandler} handleCraeteCardError={createCardErrorHandler}
    handleUpdateCardComplete={updateCardCompleteHandler} handleUpdateCardCancel={updateCardCancelHandler} handleUpdateCardError={updateCardErrorHandler}
    handleDeleteCardComplete={deleteCardCompleteHandler}/>)}
                </List>
            </div>
        </div>
  )
}

export default WorkspaceDetails
