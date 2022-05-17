import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import { ACCESS_TOKEN } from '../constants'
import Card from './Card'
import ProfileMenu from './ProfileMenu'
import AppBar from '@material-ui/core/AppBar'
import Box from '@material-ui/core/Box'
import Drawer from '@material-ui/core/Drawer'
import Divider from '@material-ui/core/Divider'
import Fab from '@material-ui/core/Fab'
import InputBase from '@material-ui/core/InputBase'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import MuiAlert from '@material-ui/lab/Alert'
import Pagination from '@material-ui/lab/Pagination'
import Toolbar from '@material-ui/core/Toolbar'
import { fade, makeStyles } from '@material-ui/core/styles'
import AddIcon from '@material-ui/icons/Add'
import DashboardIcon from '@material-ui/icons/Dashboard'
import CloseIcon from '@material-ui/icons/Close'
import ListItemIcon from '@material-ui/core/ListItemIcon'
import RocketIcon from '@material-ui/icons/EmojiEvents'
import SaveAltIcon from '@material-ui/icons/SaveAlt'
import SearchIcon from '@material-ui/icons/Search'

function Alert (props) {
  return <MuiAlert elevation={6} variant="filled" {...props} />
}

const WorkspaceDetails = (props) => {
  const params = useParams()
  const [cards, setCards] = useState([])
  const [pagesCount, setPagesCount] = useState(0)
  const [newCardStatus, setNewCardStatus] = useState(false)
  const [backupCards, setBackupCards] = useState([])
  const [searchPattern, setSearchPattern] = useState('')
  const [typingTimeout, setTypingTimeout] = useState()
  const [workspaceDetailsError, setWorkspaceDetailsError] = useState(false)
  const [workspaceDetailsErrorMessage, setWorkspaceDetailsErrorMessage] = useState('')
  const paginationSize = 5

  const getPagesCount = (cardsCount) => {
    return Math.ceil(cardsCount / paginationSize)
  }

  const getCards = async (signal) => {
    const headers = {
      Accepted: 'application/json'
    }
    if (localStorage.getItem(ACCESS_TOKEN)) {
      headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
    }
    const response = await fetch('/api/workspaces/' + params.name + '/cards?page=0&size=' + paginationSize, {
      method: 'GET',
      headers: headers,
      signal
    })
    if (!response.ok) {
      throw new Error(JSON.stringify(response))
    }
    const responseData = await response.json()
    const cardsCount = response.headers.get('count')
    const pagesCount = getPagesCount(cardsCount)
    setPagesCount(pagesCount)
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
      .then(() => setWorkspaceDetailsError(false))
      .catch((err) => {
        console.log('Error while retrieving the cards from the Workspace ' + params.name + ': ' + err.message)
        setWorkspaceDetailsError(true)
        setWorkspaceDetailsErrorMessage('Cannot retrieve the Workspace details, please refresh the page.')
      })
    return () => controller.abort()
  }, [])

  const handlePaginationChange = (event, value) => {
    const getCards = async () => {
      const headers = {
        Accepted: 'application/json'
      }
      if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
      }
      const page = value - 1
      const response = await fetch('/api/workspaces/' + params.name + '/cards?page=' + page + '&size=' + paginationSize, {
        method: 'GET',
        headers: headers
      })
      if (!response.ok) {
        throw new Error(JSON.stringify(response))
      }
      const responseData = await response.json()
      const cardsCount = response.headers.get('count')
      const pagesCount = getPagesCount(cardsCount)
      setPagesCount(pagesCount)
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
    getCards()
      .then(() => setWorkspaceDetailsError(false))
      .catch((err) => {
        console.log('Error while retrieving the cards from the Workspace ' + params.name + ': ' + err.message)
        setWorkspaceDetailsError(true)
        setWorkspaceDetailsErrorMessage('Cannot retrieve the Cards, please refresh the page.')
      })
  }

  const resetSearchHandler = () => {
    setSearchPattern('')
    setCards(backupCards)
  }

  const searchOnChangeHandler = (event) => {
    const getSearchCards = async () => {
      const headers = {
        Accepted: 'application/json'
      }
      if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
      }
      const content = encodeURIComponent(event.target.value)
      const response = await fetch('/api/workspaces/' + params.name + '/search?content=' + content, {
        method: 'GET',
        headers: headers
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
        .then(() => setWorkspaceDetailsError(false))
        .catch((err) => {
          console.log('Error while searching the Cards: ' + err.message)
          setWorkspaceDetailsError(true)
          setWorkspaceDetailsErrorMessage('Cannot search the Cards.')
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
      const headers = {
        Accepted: 'application/octet-stream'
      }
      if (localStorage.getItem(ACCESS_TOKEN)) {
        headers.Authorization = 'Bearer ' + localStorage.getItem(ACCESS_TOKEN)
      }
      const response = await fetch('/api/workspaces/' + params.name + '/export', {
        method: 'GET',
        headers: headers
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
      .then(() => setWorkspaceDetailsError(false))
      .catch((err) => {
        console.log('Error while exporting the backup: ' + err.message)
        setWorkspaceDetailsError(true)
        setWorkspaceDetailsErrorMessage('Cannot export the Workspace backup.')
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
    search: {
      position: 'relative',
      borderRadius: theme.shape.borderRadius,
      backgroundColor: fade(theme.palette.common.white, 0.15),
      '&:hover': {
        backgroundColor: fade(theme.palette.common.white, 0.25)
      },
      marginRight: theme.spacing(2),
      marginLeft: 0,
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
      },
      marginRight: 0
    },
    errors: {
      marginBottom: theme.spacing(2)
    },
    title: {
      flex: 0,
      position: 'absolute',
      alignItems: 'center',
      fontSize: 'x-large',
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
                    <Box className={classes.search}>
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
                    </Box>
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
                    <ListItem button key="Workspaces" onClick={handleExport} className={classes.drawerListItem}>
                        <ListItemIcon><SaveAltIcon /></ListItemIcon>
                    </ListItem>
                </List>
            </Drawer>
            <Box className={classes.content}>
                {workspaceDetailsError && (<div className={classes.errors}><Alert severity="error">{workspaceDetailsErrorMessage}</Alert></div>)}
                <div className={classes.title}>Cards</div>
                <Box className={classes.events}>
                  <Box className={classes.eventIcon}>
                    <Fab size="small" color="primary" aria-label="add" onClick={newCardHandler} disabled={newCardStatus}>
                        <AddIcon />
                    </Fab>
                  </Box>
                  <Box className={classes.eventIcon}>
                    <Fab size="small" color="primary" aria-label="add" component={Link} to={'/workspaces/' + params.name + '/study'}>
                        <RocketIcon />
                    </Fab>
                  </Box>
                </Box>
                <List component="nav" aria-label="cards">
                    {cards.map(card => <Card key={card.id} workspaceName={params.name} id={card.id} question={card.question} response={card.response} selected={false} new={card.new} change={card.change}
    handleCreateCard={createCardHandler} handleCreateCardCancel={createCardCancelHandler} handleUpdateCard={updateCardHandler} handleCraeteCardError={createCardErrorHandler}
    handleUpdateCardComplete={updateCardCompleteHandler} handleUpdateCardCancel={updateCardCancelHandler} handleUpdateCardError={updateCardErrorHandler}
    handleDeleteCardComplete={deleteCardCompleteHandler}/>)}
                </List>
                <Pagination count={pagesCount} defaultPage={1} siblingCount={0} boundaryCount={2} onChange={handlePaginationChange} />
            </Box>
        </Box>
  )
}

export default WorkspaceDetails
