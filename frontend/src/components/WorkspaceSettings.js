import React, { useState } from 'react'
import Box from '@material-ui/core/Box'
import { ColorPicker } from 'material-ui-color'
import Fab from '@material-ui/core/Fab'
import List from '@material-ui/core/List'
import ListItem from '@material-ui/core/ListItem'
import Grid from '@material-ui/core/Grid'
import MenuItem from '@material-ui/core/MenuItem'
import MenuList from '@material-ui/core/MenuList'
import Paper from '@material-ui/core/Paper'
import Typography from '@material-ui/core/Typography'
import { makeStyles } from '@material-ui/core/styles'
import CloseIcon from '@material-ui/icons/Close'

const WorkspaceSettings = (props) => {
  const displayPage = 'display'
  const [settingsPage, setSettingsPage] = useState(displayPage)
  const [cardQuestionTextColor, setCardQuestionTextColor] = useState('#0000008A')
  const [cardResponseTextColor, setCardResponseTextColor] = useState('#0000008A')
  const [selectedMenuIndex, setSelectedMenuIndex] = useState(0)
  const options = ['Display']

  const handleOpenMenu = (event, index) => {
    setSelectedMenuIndex(index)
    if (index === 0) {
      setSettingsPage(displayPage)
    }
  }

  const handlePickCardQuestionTextColor = (event) => {
    setCardQuestionTextColor('#' + event.hex)
    document.documentElement.style.setProperty('--card-question-text-color', '#' + event.hex)
  }

  const handlePickCardResponseTextColor = (event) => {
    setCardResponseTextColor('#' + event.hex)
    document.documentElement.style.setProperty('--card-response-text-color', '#' + event.hex)
  }

  const useStyles = makeStyles((theme) => ({
    content: {
      marginRight: 0,
      width: '100%'
    },
    drawerList: {
      padding: theme.spacing(0, 0)
    },
    settingsContent: {
      padding: theme.spacing(2, 0)
    },
    title: {
      flex: 0,
      position: 'absolute',
      alignItems: 'center',
      fontSize: 'x-large',
      padding: theme.spacing(0, 1)
    },
    paper: {
      marginRight: theme.spacing(2),
      '@media only screen and (max-width:768px)': {
        marginRight: theme.spacing(0)
      }
    },
    closeIcon: {
      marginLeft: 'auto',
      marginRight: theme.spacing(2)
    },
    settingsMenu: {
      padding: theme.spacing(0, 0)
    },
    settingsMenuContent: {
      padding: theme.spacing(0, 0),
      '@media only screen and (max-width:768px)': {
        display: 'inline-block'
      }
    },
    settingsConfiguration: {
      padding: theme.spacing(0, 5),
      '@media only screen and (max-width:768px)': {
        padding: theme.spacing(0, 1)
      }
    },
    settingsGridRow: {
      width: '100%',
      padding: theme.spacing(2, 0)
    },
    settingsGrid: {
      width: '100%'
    }
  }))
  const classes = useStyles()

  return (
        <div className={classes.content}>
          <List className={classes.drawerList}>
            <ListItem className={classes.drawerList}>
              <Box className={classes.title}>Settings</Box>
              <Box className={classes.closeIcon}>
                <Fab size="small" color="primary" aria-label="avatar" onClick={props.handleClose} aria-haspopup="true">
                  <CloseIcon />
                </Fab>
              </Box>
            </ListItem>
            <ListItem className={classes.settingsContent}>
              <Grid container spacing={0}>
                <Grid item spacing={0} className={classes.settingsMenu}>
                  <Paper className={classes.paper}>
                    {options.map((option, index) => (
                      <MenuList key={option} className={classes.settingsMenuContent}>
                          <MenuItem selected={index === selectedMenuIndex} onClick={(event) => handleOpenMenu(event, index)}>{option}</MenuItem>
                      </MenuList>
                    ))}
                  </Paper>
                </Grid>
                {
                    settingsPage === displayPage && (
                        <Grid item spacing={1} className={classes.settingsConfiguration}>
                            <Grid container spacing={1}>
                              <Grid container spacing={2} className={classes.settingsGridRow}>
                                  <Grid item className={classes.settingsGrid}>
                                    <Typography id="contrast-slider" gutterBottom>
                                      Card Question text color
                                    </Typography>
                                    <ColorPicker defaultValue={'#0000008A'} value={cardQuestionTextColor} onChange={handlePickCardQuestionTextColor} deferred />
                                  </Grid>
                              </Grid>
                              <Grid container spacing={2} className={classes.settingsGridRow}>
                                <Grid item className={classes.settingsGrid}>
                                  <Typography id="contrast-slider" gutterBottom>
                                    Card Response text color
                                  </Typography>
                                  <ColorPicker defaultValue={'#0000008A'} value={cardResponseTextColor} onChange={handlePickCardResponseTextColor} deferred />
                                </Grid>
                              </Grid>
                            </Grid>
                        </Grid>
                    )
                }
              </Grid>
            </ListItem>
          </List>
        </div>
  )
}

export default WorkspaceSettings
