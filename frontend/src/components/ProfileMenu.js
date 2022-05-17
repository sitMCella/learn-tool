import React, { useState } from 'react'
import { Link } from 'react-router-dom'
import Box from '@material-ui/core/Box'
import Fab from '@material-ui/core/Fab'
import Avatar from '@material-ui/core/Avatar'
import Popper from '@material-ui/core/Popper'
import Grow from '@material-ui/core/Grow'
import Paper from '@material-ui/core/Paper'
import ClickAwayListener from '@material-ui/core/ClickAwayListener'
import MenuList from '@material-ui/core/MenuList'
import MenuItem from '@material-ui/core/MenuItem'
import { makeStyles } from '@material-ui/core/styles'

function ProfileMenu (props) {
  const [avatarMenuOpen, setAvatarMenuOpen] = useState(false)
  const [anchorEl, setAnchorEl] = useState(null)

  const avatarClickHandler = (event) => {
    setAvatarMenuOpen((previousOpen) => !previousOpen)
    setAnchorEl(event.currentTarget)
  }

  const avatarMenuCloseHandler = () => {
    setAvatarMenuOpen(false)
    setAnchorEl(null)
  }

  const logoutHandler = () => {
    props.onLogout()
    props.history.push({
      pathname: '/login',
      key: Math.random()
    })
  }

  const useStyles = makeStyles((theme) => ({
    profileMenu: {
      flex: 1,
      display: 'flex',
      justifyContent: 'flex-end',
      paddingRight: 5,
      alignItems: 'center'
    },
    avatarIcon: {
      width: theme.spacing(4),
      height: theme.spacing(4)
    }
  }))
  const classes = useStyles()

  return (
        <div className={classes.profileMenu}>
            <Box>
                <Fab size="small" color="primary" aria-label="avatar" onClick={avatarClickHandler} aria-controls={avatarMenuOpen ? 'menu-list-grow' : undefined} aria-haspopup="true">
                    <Avatar className={classes.avatarIcon} />
                </Fab>
            </Box>
            <Popper open={avatarMenuOpen} anchorEl={anchorEl} role={undefined} transition disablePortal>
                {({ TransitionProps, placement }) => (
                    <Grow
                        {...TransitionProps}
                        style={{ transformOrigin: placement === 'bottom' ? 'center top' : 'center bottom' }}>
                        <Paper>
                            <ClickAwayListener onClickAway={avatarMenuCloseHandler}>
                                <MenuList autoFocusItem={avatarMenuOpen} id="avatar-menu" anchorEl={anchorEl} keepMounted open={Boolean(anchorEl)} onClose={avatarMenuCloseHandler}>
                                    <MenuItem component={Link} to="/profile">Profile</MenuItem>
                                    <MenuItem onClick={logoutHandler}>Logout</MenuItem>
                                </MenuList>
                            </ClickAwayListener>
                        </Paper>
                    </Grow>
                )}
            </Popper>
        </div>
  )
}

export default ProfileMenu
